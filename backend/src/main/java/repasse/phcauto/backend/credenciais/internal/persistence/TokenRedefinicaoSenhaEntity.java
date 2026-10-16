package repasse.phcauto.backend.credenciais.internal.persistence;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.infra.database.sync.WriteOnlyOperationalData;
@Entity @WriteOnlyOperationalData
@Table(name="tokens_redefinicao_senha",schema="identidade") @Access(AccessType.FIELD)
public class TokenRedefinicaoSenhaEntity {
    @Id private UUID id;
    @Column(name="usuario_id",nullable=false) private UUID usuarioId;
    @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash;
    @Column(name="criado_em",nullable=false) private Instant criadoEm;
    @Column(name="expira_em",nullable=false) private Instant expiraEm;
    @Column(name="usado_em") private Instant usadoEm;
    @Column(name="revogado_em") private Instant revogadoEm;
    @Column(name="solicitado_ip_hash",length=64) private String solicitadoIpHash;
    @Version @Column(name="lock_version",nullable=false) private long lockVersion;
    protected TokenRedefinicaoSenhaEntity(){}
    public static TokenRedefinicaoSenhaEntity criar(UUID usuario,String hash,String ipHash,Instant agora,Instant expira){
        var e=new TokenRedefinicaoSenhaEntity();e.id=UUID.randomUUID();e.usuarioId=usuario;e.tokenHash=hash;
        e.solicitadoIpHash=ipHash;e.criadoEm=agora;e.expiraEm=expira;return e;}
    public boolean valido(Instant agora){return usadoEm==null&&revogadoEm==null&&expiraEm.isAfter(agora);}
    public void usar(Instant agora){usadoEm=agora;}
    public UUID getUsuarioId(){return usuarioId;}
}
