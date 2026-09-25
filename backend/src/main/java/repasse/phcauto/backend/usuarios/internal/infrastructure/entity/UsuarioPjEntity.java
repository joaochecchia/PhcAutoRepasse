package repasse.phcauto.backend.usuarios.internal.infrastructure.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.UsuarioPj;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "usuarios_pj", schema = "identidade")
@Access(AccessType.FIELD)
public class UsuarioPjEntity extends UsuarioPj {

    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cnpj", nullable = false, length = 14, columnDefinition = "char(14)")
    private String cnpj;

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = true, length = 200)
    private String nomeFantasia;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected UsuarioPjEntity() { }

    public static UsuarioPjEntity criar(UsuarioPj dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new UsuarioPjEntity();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.cnpj = Objects.requireNonNull(dados.getCnpj(), "cnpj");
        entity.razaoSocial = Objects.requireNonNull(dados.getRazaoSocial(), "razaoSocial");
        entity.nomeFantasia = dados.getNomeFantasia();
        return entity;
    }

    public void atualizar(UsuarioPj dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(usuarioId, dados.getUsuarioId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.cnpj = Objects.requireNonNull(dados.getCnpj(), "cnpj");
        this.razaoSocial = Objects.requireNonNull(dados.getRazaoSocial(), "razaoSocial");
        this.nomeFantasia = dados.getNomeFantasia();
    }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public String getCnpj() { return cnpj; }

    @Override
    public String getRazaoSocial() { return razaoSocial; }

    @Override
    public String getNomeFantasia() { return nomeFantasia; }

    public Long getLockVersion() { return lockVersion; }

    public void aplicarPatch(String cnpj, String razaoSocial, String nomeFantasia) {
        if (cnpj != null) this.cnpj = cnpj;
        if (razaoSocial != null) this.razaoSocial = razaoSocial;
        if (nomeFantasia != null) this.nomeFantasia = nomeFantasia;
    }

    public static UsuarioPjEntity novoCadastro(UUID id, repasse.phcauto.backend.usuarios.internal.core.DadosNovoUsuario dados, java.time.Instant agora) {
        var entity = new UsuarioPjEntity();
        entity.usuarioId = id;
        entity.cnpj = dados.cnpj();
        entity.razaoSocial = dados.razaoSocial();
        entity.nomeFantasia = dados.nome();
        return entity;
    }
}
