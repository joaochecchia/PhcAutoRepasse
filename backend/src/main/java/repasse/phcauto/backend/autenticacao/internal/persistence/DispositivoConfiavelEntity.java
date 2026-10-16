package repasse.phcauto.backend.autenticacao.internal.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.infra.database.sync.WriteOnlyOperationalData;

@Entity
@WriteOnlyOperationalData
@Table(name = "dispositivos_confiaveis", schema = "identidade")
@Access(AccessType.FIELD)
public class DispositivoConfiavelEntity {
    @Id private UUID id;
    @Column(name="usuario_id", nullable=false) private UUID usuarioId;
    @Column(name="token_hash", nullable=false, unique=true, length=64) private String tokenHash;
    @Column(name="user_agent_hash", length=64) private String userAgentHash;
    @Column(name="nome_dispositivo", length=120) private String nomeDispositivo;
    @Column(name="versao_seguranca", nullable=false) private long versaoSeguranca;
    @Column(name="criado_em", nullable=false) private Instant criadoEm;
    @Column(name="ultimo_uso_em", nullable=false) private Instant ultimoUsoEm;
    @Column(name="expira_em", nullable=false) private Instant expiraEm;
    @Column(name="revogado_em") private Instant revogadoEm;
    @Version @Column(name="lock_version", nullable=false) private long lockVersion;
    protected DispositivoConfiavelEntity() { }
    public static DispositivoConfiavelEntity criar(UUID usuario, String hash, String agenteHash,
            String nome, long versao, Instant agora, Instant expira) {
        var e=new DispositivoConfiavelEntity(); e.id=UUID.randomUUID(); e.usuarioId=usuario;
        e.tokenHash=hash; e.userAgentHash=agenteHash; e.nomeDispositivo=nome; e.versaoSeguranca=versao;
        e.criadoEm=agora; e.ultimoUsoEm=agora; e.expiraEm=expira; return e;
    }
    public boolean valida(UUID usuario, long versao, String agenteHash, Instant agora) {
        return usuarioId.equals(usuario) && versaoSeguranca==versao && revogadoEm==null
                && expiraEm.isAfter(agora) && java.util.Objects.equals(userAgentHash, agenteHash);
    }
    public void usado(Instant agora) { ultimoUsoEm=agora; }
    public void revogar(Instant agora) { if(revogadoEm==null) revogadoEm=agora; }
    public UUID getId(){return id;} public UUID getUsuarioId(){return usuarioId;}
    public String getTokenHash(){return tokenHash;} public String getNomeDispositivo(){return nomeDispositivo;}
    public Instant getCriadoEm(){return criadoEm;} public Instant getUltimoUsoEm(){return ultimoUsoEm;}
    public Instant getExpiraEm(){return expiraEm;} public Instant getRevogadoEm(){return revogadoEm;}
}
