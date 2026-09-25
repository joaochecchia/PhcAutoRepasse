package repasse.phcauto.backend.usuarios.internal.infrastructure.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.UsuarioPf;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "usuarios_pf", schema = "identidade")
@Access(AccessType.FIELD)
public class UsuarioPfEntity extends UsuarioPf {

    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cpf", nullable = false, length = 11, columnDefinition = "char(11)")
    private String cpf;

    @Column(name = "data_nascimento", nullable = true)
    private LocalDate dataNascimento;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected UsuarioPfEntity() { }

    public static UsuarioPfEntity criar(UsuarioPf dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new UsuarioPfEntity();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.cpf = Objects.requireNonNull(dados.getCpf(), "cpf");
        entity.dataNascimento = dados.getDataNascimento();
        return entity;
    }

    public void atualizar(UsuarioPf dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(usuarioId, dados.getUsuarioId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.cpf = Objects.requireNonNull(dados.getCpf(), "cpf");
        this.dataNascimento = dados.getDataNascimento();
    }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public String getCpf() { return cpf; }

    @Override
    public LocalDate getDataNascimento() { return dataNascimento; }

    public Long getLockVersion() { return lockVersion; }

    public void aplicarPatch(String cpf, LocalDate dataNascimento) {
        if (cpf != null) this.cpf = cpf;
        if (dataNascimento != null) this.dataNascimento = dataNascimento;
    }

    public static UsuarioPfEntity novoCadastro(UUID id, repasse.phcauto.backend.usuarios.internal.core.DadosNovoUsuario dados, java.time.Instant agora) {
        var entity = new UsuarioPfEntity();
        entity.usuarioId = id;
        entity.cpf = dados.cpf();
        entity.dataNascimento = dados.dataNascimento();
        return entity;
    }
}
