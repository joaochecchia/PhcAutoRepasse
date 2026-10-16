package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence;
import java.util.*;
import java.time.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import jakarta.persistence.*;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.*;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.*;
@Repository
@Transactional(transactionManager="writeTransactionManager",readOnly=true)
public class JpaCatalogoFipeGateway implements CatalogoFipeGateway {
    private final FipeReferenciaRepository referencias;
    private final Clock clock;
    @PersistenceContext(unitName="write") private EntityManager em;
    public JpaCatalogoFipeGateway(FipeReferenciaRepository referencias,Clock clock){this.referencias=referencias;this.clock=clock;}
    public Optional<ReferenciaFipe> buscar(UUID id){return referencias.buscarAtiva(id).map(FipeReferenciaEntity::referencia);}
    public Optional<ReferenciaFipe> buscarConsulta(String chave,LocalDate mes){return referencias.buscarConsulta(chave,mes,PageRequest.of(0,1)).stream().findFirst().map(FipeReferenciaEntity::referencia);}
    @Transactional(transactionManager="writeTransactionManager")
    public ReferenciaFipe salvarConsulta(DadosReferencia d,String chave){
        // Lock transacional somente local; HTTP já terminou. Unicidade cobre processos concorrentes.
        em.createNativeQuery("select pg_advisory_xact_lock(hashtextextended(:chave,0))")
            .setParameter("chave",d.codigoFipe()+":"+d.anoModelo()+":"+d.mesReferencia()).getSingleResult();
        return referencias.buscarExterna(d.tipo(),d.codigoFipe(),d.anoModelo(),d.combustivel(),d.mesReferencia())
            .orElseGet(()->referencias.save(FipeReferenciaEntity.criar(d,"PARALLELUM",chave,null,clock.instant()))).referencia();
    }
    public List<ReferenciaFipe> pesquisar(String tipo,String marca,String termo,Integer ano,LocalDate mes,int pagina,int tamanho){
        String busca=termo==null?null:"%"+termo.toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        return referencias.pesquisar(tipo,marca==null?null:marca.toLowerCase(Locale.ROOT),busca,ano,mes,PageRequest.of(pagina,tamanho)).stream().map(FipeReferenciaEntity::referencia).toList();
    }
}
