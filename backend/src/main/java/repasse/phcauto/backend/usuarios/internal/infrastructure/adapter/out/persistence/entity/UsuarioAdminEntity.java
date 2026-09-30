package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.domain.model.identidade.UsuarioAdministrador;
@Entity
@Table(name = "usuarios_admin", schema = "identidade")
@Access(AccessType.FIELD)
public class UsuarioAdminEntity extends UsuarioAdministrador {
    @Id @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_pessoa", nullable = false, length = 32)
    private TipoPessoa tipoPessoa;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cpf", length = 11, columnDefinition = "char(11)") private String cpf;
    @Column(name = "data_nascimento") private LocalDate dataNascimento;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cnpj", length = 14, columnDefinition = "char(14)") private String cnpj;
    @Column(name = "razao_social", length = 200) private String razaoSocial;
    @Version @Column(name = "lock_version", nullable = false) private Long lockVersion;

    protected UsuarioAdminEntity() { }
    public static UsuarioAdminEntity novo(UUID usuarioId, TipoPessoa tipoPessoa, String cpf,
            LocalDate nascimento, String cnpj, String razaoSocial) {
        var e = new UsuarioAdminEntity();
        e.usuarioId = Objects.requireNonNull(usuarioId);
        e.tipoPessoa = Objects.requireNonNull(tipoPessoa);
        e.cpf = cpf; e.dataNascimento = nascimento; e.cnpj = cnpj; e.razaoSocial = razaoSocial;
        return e;
    }
    public UUID getUsuarioId() { return usuarioId; }
    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public String getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getCnpj() { return cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public Long getLockVersion() { return lockVersion; }
}
