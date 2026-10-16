package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
@Entity @Table(schema="fipe", name="importacoes")
public class FipeImportacaoEntity {
    @Id private UUID id;
    @Column(nullable=false, length=64) private String checksum;
    @Column(nullable=false, length=120) private String fonte;
    @Column(name="mes_referencia",nullable=false) private LocalDate mesReferencia;
    @Column(nullable=false, length=20) private String status;
    @Column(name="posicao_bytes",nullable=false) private long posicaoBytes;
    @Column(nullable=false) private long registros;
    @Column(length=500) private String erro;
    @Column(name="criado_em",nullable=false) private Instant criadoEm;
    @Column(name="atualizado_em",nullable=false) private Instant atualizadoEm;
    @Version @Column(name="lock_version",nullable=false) private Long lockVersion;
    protected FipeImportacaoEntity() { }
    public static FipeImportacaoEntity criar(UUID id,String checksum,String fonte,LocalDate mes,Instant agora) {
        var e=new FipeImportacaoEntity();e.id=id;e.checksum=checksum;e.fonte=fonte;e.mesReferencia=mes;
        e.status="PENDENTE";e.criadoEm=agora;e.atualizadoEm=agora;return e;
    }
    public void avancar(long posicao,long quantidade,boolean fim,Instant agora) {
        posicaoBytes=posicao;registros+=quantidade;status=fim?"CONCLUIDA":"PROCESSANDO";
        atualizadoEm=agora;erro=null;
    }
    public void falhar(Instant agora) {if ("CONCLUIDA".equals(status)) return; status="FALHOU";erro="Arquivo inválido ou falha no processamento; consulte o log pelo ID da importação";atualizadoEm=agora;}
    public void retomar(Instant agora) {if(!"FALHOU".equals(status))throw new IllegalStateException("Somente importações com falha podem ser retomadas");status="PENDENTE";erro=null;atualizadoEm=agora;}
    public UUID getId(){return id;} public String getFonte(){return fonte;} public LocalDate getMesReferencia(){return mesReferencia;}
    public String getStatus(){return status;} public long getPosicaoBytes(){return posicaoBytes;}
    public long getRegistros(){return registros;} public String getErro(){return erro;}
}
