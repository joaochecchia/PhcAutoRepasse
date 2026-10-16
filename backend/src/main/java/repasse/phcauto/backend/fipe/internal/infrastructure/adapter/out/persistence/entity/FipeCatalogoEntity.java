package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
import repasse.phcauto.backend.fipe.internal.core.domain.OpcaoFipe;
@Entity @Table(schema="fipe",name="catalogo")
public class FipeCatalogoEntity {
    @Id private UUID id;
    @Column(name="pai_id") private UUID paiId;
    @Column(nullable=false,length=100) private String escopo;
    @Column(name="codigo_externo",nullable=false,length=20) private String codigoExterno;
    @Column(nullable=false,length=250) private String nome;
    @Column(name="mes_referencia") private LocalDate mesReferencia;
    @Column(name="atualizado_em",nullable=false) private Instant atualizadoEm;
    @Version @Column(name="lock_version") private Long version;
    protected FipeCatalogoEntity() {}
    public static FipeCatalogoEntity criar(String escopo,UUID pai,OpcaoFipe o,Instant agora) {
        var e=new FipeCatalogoEntity();e.id=UUID.randomUUID();e.escopo=escopo;e.paiId=pai;e.codigoExterno=o.codigo();e.atualizar(o,agora);return e;
    }
    public void atualizar(OpcaoFipe o,Instant agora) {nome=o.nome();mesReferencia=o.mesReferencia();atualizadoEm=agora;}
    public UUID getId(){return id;}
    public OpcaoFipe opcao(){return new OpcaoFipe(codigoExterno,nome,mesReferencia);}
}
