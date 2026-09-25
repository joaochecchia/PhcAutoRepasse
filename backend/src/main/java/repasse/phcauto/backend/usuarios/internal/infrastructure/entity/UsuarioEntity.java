package repasse.phcauto.backend.usuarios.internal.infrastructure.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.domain.model.identidade.Usuario;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "usuarios", schema = "identidade")
@Access(AccessType.FIELD)
public class UsuarioEntity extends Usuario {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "senha_hash", nullable = true, columnDefinition = "text")
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 32)
    private TipoPessoa tipoPessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 32)
    private PapelUsuario papel;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "telefone", length = 32)
    private String telefone;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected UsuarioEntity() { }

    public static UsuarioEntity criar(Usuario dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new UsuarioEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.nome = Objects.requireNonNull(dados.getNome(), "nome");
        entity.telefone = dados.getTelefone();
        entity.email = Objects.requireNonNull(dados.getEmail(), "email");
        entity.senhaHash = dados.getSenhaHash();
        entity.tipoPessoa = Objects.requireNonNull(dados.getTipoPessoa(), "tipoPessoa");
        entity.papel = Objects.requireNonNull(dados.getPapel(), "papel");
        entity.ativo = dados.getAtivo();
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        entity.atualizadoEm = Objects.requireNonNull(dados.getAtualizadoEm(), "atualizadoEm");
        return entity;
    }

    public void atualizar(Usuario dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.nome = Objects.requireNonNull(dados.getNome(), "nome");
        this.telefone = dados.getTelefone();
        this.email = Objects.requireNonNull(dados.getEmail(), "email");
        this.senhaHash = dados.getSenhaHash();
        this.tipoPessoa = Objects.requireNonNull(dados.getTipoPessoa(), "tipoPessoa");
        this.papel = Objects.requireNonNull(dados.getPapel(), "papel");
        this.ativo = dados.getAtivo();
        this.atualizadoEm = Objects.requireNonNull(dados.getAtualizadoEm(), "atualizadoEm");
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public String getNome() { return nome; }

    @Override
    public String getEmail() { return email; }

    @Override
    public String getSenhaHash() { return senhaHash; }

    @Override
    public TipoPessoa getTipoPessoa() { return tipoPessoa; }

    @Override
    public PapelUsuario getPapel() { return papel; }

    @Override
    public boolean getAtivo() { return ativo; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    @Override
    public Instant getAtualizadoEm() { return atualizadoEm; }

    @Override
    public String getTelefone() { return telefone; }

    public Long getLockVersion() { return lockVersion; }

    public void aplicarPatch(String nome, String email, String telefone, String senhaHash,
            Instant atualizadoEm) {
        if (nome != null) this.nome = nome;
        if (email != null) this.email = email;
        if (telefone != null) this.telefone = telefone;
        if (senhaHash != null) this.senhaHash = senhaHash;
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "atualizadoEm");
    }

    public static UsuarioEntity novoCadastro(UUID id, repasse.phcauto.backend.usuarios.internal.core.DadosNovoUsuario dados, java.time.Instant agora) {
        var entity = new UsuarioEntity();
        entity.id = id;
        entity.nome = dados.nome();
        entity.email = dados.email();
        entity.telefone = dados.telefone();
        entity.senhaHash = dados.senhaHash();
        entity.tipoPessoa = dados.tipoPessoa();
        entity.papel = PapelUsuario.CLIENTE;
        entity.ativo = true;
        entity.criadoEm = agora;
        entity.atualizadoEm = agora;
        return entity;
    }
}
