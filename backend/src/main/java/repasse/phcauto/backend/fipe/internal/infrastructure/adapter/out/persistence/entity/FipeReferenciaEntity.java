package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
@Entity @Table(schema="fipe", name="referencias")
public class FipeReferenciaEntity {
    @Id private UUID id;
    @Column(name="codigo_fipe", nullable=false, length=16) private String codigoFipe;
    @Column(nullable=false, length=16) private String tipo;
    @Column(nullable=false, length=100) private String marca;
    @Column(nullable=false, length=250) private String modelo;
    @Column(name="ano_modelo", nullable=false) private int anoModelo;
    @Column(nullable=false, length=60) private String combustivel;
    @Column(name="mes_referencia", nullable=false) private LocalDate mesReferencia;
    @Column(name="valor_centavos", nullable=false) private long valorCentavos;
    @Column(nullable=false, length=120) private String fonte;
    @Column(name="chave_consulta", length=100) private String chaveConsulta;
    @Column(name="importacao_id") private UUID importacaoId;
    @Column(name="criado_em", nullable=false) private Instant criadoEm;
    @Version @Column(name="lock_version", nullable=false) private Long lockVersion;
    protected FipeReferenciaEntity() { }
    public static FipeReferenciaEntity criar(DadosReferencia d, String fonte, String chave, UUID importacao, Instant agora) {
        var e = new FipeReferenciaEntity(); e.id=UUID.randomUUID(); e.codigoFipe=d.codigoFipe();
        e.tipo=d.tipo(); e.marca=d.marca(); e.modelo=d.modelo(); e.anoModelo=d.anoModelo();
        e.combustivel=d.combustivel(); e.mesReferencia=d.mesReferencia(); e.valorCentavos=d.valorCentavos();
        e.fonte=fonte; e.chaveConsulta=chave; e.importacaoId=importacao; e.criadoEm=agora; return e;
    }
    public UUID getId() { return id; }
    public ReferenciaFipe referencia() { return new ReferenciaFipe(id,codigoFipe,tipo,marca,modelo,anoModelo,
            anoModelo==32000,combustivel,mesReferencia,valorCentavos,fonte); }
}
