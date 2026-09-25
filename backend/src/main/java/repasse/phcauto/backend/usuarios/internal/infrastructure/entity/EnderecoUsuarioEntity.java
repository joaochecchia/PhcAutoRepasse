package repasse.phcauto.backend.usuarios.internal.infrastructure.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.EnderecoUsuario;

@Entity
@Table(name = "enderecos_usuario", schema = "identidade")
@Access(AccessType.FIELD)
public class EnderecoUsuarioEntity extends EnderecoUsuario {
    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "cep", nullable = true, length = 8)
    private String cep;

    @Column(name = "cidade", nullable = true, length = 120)
    private String cidade;

    @Column(name = "bairro", nullable = true, length = 120)
    private String bairro;

    @Column(name = "rua", nullable = true, length = 200)
    private String rua;

    @Column(name = "numero", nullable = true, length = 20)
    private String numero;

    @Column(name = "complemento", nullable = true, length = 200)
    private String complemento;

    @Column(name = "uf", nullable = true, length = 2)
    private String uf;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected EnderecoUsuarioEntity() { }

    public static EnderecoUsuarioEntity criar(EnderecoUsuario dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new EnderecoUsuarioEntity();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.cep = dados.getCep();
        entity.cidade = dados.getCidade();
        entity.bairro = dados.getBairro();
        entity.rua = dados.getRua();
        entity.numero = dados.getNumero();
        entity.complemento = dados.getComplemento();
        entity.uf = dados.getUf();
        return entity;
    }

    public void atualizar(EnderecoUsuario dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(usuarioId, dados.getUsuarioId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.cep = dados.getCep();
        this.cidade = dados.getCidade();
        this.bairro = dados.getBairro();
        this.rua = dados.getRua();
        this.numero = dados.getNumero();
        this.complemento = dados.getComplemento();
        this.uf = dados.getUf();
    }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public String getCep() { return cep; }

    @Override
    public String getCidade() { return cidade; }

    @Override
    public String getBairro() { return bairro; }

    @Override
    public String getRua() { return rua; }

    @Override
    public String getNumero() { return numero; }

    @Override
    public String getComplemento() { return complemento; }

    @Override
    public String getUf() { return uf; }

    public Long getLockVersion() { return lockVersion; }

    public void aplicarPatch(repasse.phcauto.backend.usuarios.internal.core.EnderecoUsuarioDados dados) {
        Objects.requireNonNull(dados, "dados");
        this.cep = dados.cep();
        this.cidade = dados.cidade();
        this.bairro = dados.bairro();
        this.rua = dados.rua();
        this.numero = dados.numero();
        this.complemento = dados.complemento();
        this.uf = dados.uf();
    }

    public static EnderecoUsuarioEntity novoCadastro(UUID id, repasse.phcauto.backend.usuarios.internal.core.DadosNovoUsuario dados, java.time.Instant agora) {
        var entity = new EnderecoUsuarioEntity();
        entity.usuarioId = id;
        entity.cep = dados.endereco().cep();
        entity.cidade = dados.endereco().cidade();
        entity.bairro = dados.endereco().bairro();
        entity.rua = dados.endereco().rua();
        entity.numero = dados.endereco().numero();
        entity.complemento = dados.endereco().complemento();
        entity.uf = dados.endereco().uf();
        return entity;
    }
}
