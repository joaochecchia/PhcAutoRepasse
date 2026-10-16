package repasse.phcauto.backend.fipe.internal.infrastructure.service;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.security.*;
import java.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.LoggerFactory;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.usecase.ImportarLoteFipeUseCase;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.FipeImportacaoEntity;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.FipeImportacaoRepository;
import static repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.*;

@Service
public class ImportacaoFipeService {
    private final FipeImportacaoRepository repos;
    private final ImportarLoteFipeUseCase importar;
    private final TransactionTemplate tx;
    private final ObjectMapper json;
    private final Clock clock;
    private final Path diretorio;
    private final MeterRegistry metrics;
    private final String fonteMensal;
    public ImportacaoFipeService(FipeImportacaoRepository repos,ImportarLoteFipeUseCase importar,
            @Qualifier("writeTransactionManager")PlatformTransactionManager tm,ObjectMapper json,Clock clock,
            MeterRegistry metrics,@Value("${app.fipe.import-directory:./data/fipe}")String diretorio,
            @Value("${app.fipe.monthly-source:IMPORTACAO_ADMINISTRATIVA}")String fonteMensal){
        this.repos=repos;this.importar=importar;this.tx=new TransactionTemplate(tm);this.json=json;this.clock=clock;
        this.diretorio=Path.of(diretorio).toAbsolutePath().normalize();this.metrics=metrics;this.fonteMensal=fonteMensal;
    }
    public record Estado(UUID id,String status,long registros,String erro) { }
    public Estado enviar(InputStream entrada,String fonte,LocalDate mes) throws IOException {
        if(fonte==null || fonte.isBlank() || fonte.length()>120 || mes==null || mes.getDayOfMonth()!=1
                || mes.getYear()<2000 || mes.isAfter(YearMonth.now(clock).atDay(1)))
            throw new FipeException(INVALIDA,"Fonte e mês de referência válidos são obrigatórios");
        Files.createDirectories(diretorio);
        UUID id=UUID.randomUUID();Path arquivo=diretorio.resolve(id+".ndjson");
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");long total=0;
            try(var out=Files.newOutputStream(arquivo,StandardOpenOption.CREATE_NEW)){
                byte[] buffer=new byte[8192];int n;
                while((n=entrada.read(buffer))!=-1){total+=n;if(total>50_000_000)throw new FipeException(INVALIDA,"Arquivo FIPE excede 50 MB");
                    out.write(buffer,0,n);digest.update(buffer,0,n);}
            }
            if(total==0)throw new FipeException(INVALIDA,"Arquivo vazio");
            String checksum=HexFormat.of().formatHex(digest.digest());
            Estado estado=tx.execute(s->{
                var existente=repos.findByChecksumAndFonteAndMesReferencia(checksum,fonte,mes);
                return estado(existente.orElseGet(()->repos.saveAndFlush(FipeImportacaoEntity.criar(id,checksum,fonte,mes,clock.instant()))));
            });
            if(!id.equals(estado.id()))Files.deleteIfExists(arquivo);
            return estado;
        }catch(NoSuchAlgorithmException e){Files.deleteIfExists(arquivo);throw new IllegalStateException(e);}
        catch(RuntimeException|IOException e){Files.deleteIfExists(arquivo);throw e;}
    }
    public Estado consultar(UUID id){return tx.execute(s->estado(repos.findById(id).orElseThrow(()->new FipeException(NAO_ENCONTRADA,"Importação não encontrada"))));}
    public Estado retomar(UUID id){return tx.execute(s->{var i=repos.findById(id).orElseThrow(()->new FipeException(NAO_ENCONTRADA,"Importação não encontrada"));
        if(!"FALHOU".equals(i.getStatus()))throw new FipeException(CONFLITO,"Somente importações com falha podem ser retomadas");
        i.retomar(clock.instant());return estado(i);});}
    @Scheduled(fixedDelayString="${app.fipe.import-delay:1000}",initialDelayString="${app.fipe.import-initial-delay:10000}")
    public void processar(){
        UUID[] id={null};
        try {
            tx.executeWithoutResult(s->{
                var proxima=repos.proximoLote();if(proxima.isEmpty())return;
                var i=proxima.get();id[0]=i.getId();
                try{
                    var lote=ArquivoImportacaoFipe.ler(diretorio.resolve(i.getId()+".ndjson"),i.getPosicaoBytes(),json);
                    if(lote.fim() && i.getRegistros()+lote.dados().size()==0)throw new FipeException(INVALIDA,"Importação sem registros");
                    importar.execute(i.getId(),i.getFonte(),i.getMesReferencia(),lote.dados());
                    i.avancar(lote.posicao(),lote.dados().size(),lote.fim(),clock.instant());
                }catch(IOException e){throw new UncheckedIOException(e);}
            });
        }catch(RuntimeException e){
            metrics.counter("phcauto.fipe.import.errors").increment();
            LoggerFactory.getLogger(getClass()).error("Falha na importação FIPE {}",id[0],e);
            if(id[0]!=null)tx.executeWithoutResult(s->repos.findById(id[0]).ifPresent(i->i.falhar(clock.instant())));
        }
    }
    /** Um fornecedor autorizado deposita YYYY-MM.ndjson em entrada; não há scraping implícito. */
    @Scheduled(cron="${app.fipe.monthly-cron:0 0 3 * * *}",zone="America/Sao_Paulo")
    public void importarArquivoDoMes(){
        var mes=YearMonth.now(clock);var arquivo=diretorio.resolve("entrada").resolve(mes+".ndjson");
        if(!Files.isRegularFile(arquivo))return;
        try(var in=Files.newInputStream(arquivo)){enviar(in,fonteMensal,mes.atDay(1));}
        catch(Exception e){metrics.counter("phcauto.fipe.import.errors").increment();LoggerFactory.getLogger(getClass()).error("Falha na entrada mensal FIPE {}",mes,e);}
    }
    private static Estado estado(FipeImportacaoEntity i){return new Estado(i.getId(),i.getStatus(),i.getRegistros(),i.getErro());}
}
