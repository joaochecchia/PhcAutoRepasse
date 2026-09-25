package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Anuncio;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "anuncios", schema = "catalogo")
@Access(AccessType.FIELD)
public class AnuncioEntity extends Anuncio {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

    @Column(name = "anunciante_id", nullable = false)
    private UUID anuncianteId;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "descricao", nullable = true, columnDefinition = "text")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_preco", nullable = false, length = 32)
    private TipoPreco tipoPreco;

    @Column(name = "preco_centavos", nullable = true)
    private Long precoCentavos;

    @Column(name = "aceita_troca", nullable = true)
    private Boolean aceitaTroca;

    @Column(name = "cidade", nullable = false, length = 120)
    private String cidade;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf", nullable = false, length = 2, columnDefinition = "char(2)")
    private String uf;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StatusAnuncio status;

    @Column(name = "publicado_em", nullable = true)
    private Instant publicadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    @Column(name = "versao", nullable = false)
    private Integer versao;

    protected AnuncioEntity() { }

    public static AnuncioEntity criar(Anuncio dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new AnuncioEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.anuncianteId = Objects.requireNonNull(dados.getAnuncianteId(), "anuncianteId");
        entity.titulo = Objects.requireNonNull(dados.getTitulo(), "titulo");
        entity.descricao = dados.getDescricao();
        entity.tipoPreco = Objects.requireNonNull(dados.getTipoPreco(), "tipoPreco");
        entity.precoCentavos = dados.getPrecoCentavos();
        entity.aceitaTroca = dados.getAceitaTroca();
        entity.cidade = Objects.requireNonNull(dados.getCidade(), "cidade");
        entity.uf = Objects.requireNonNull(dados.getUf(), "uf");
        entity.status = Objects.requireNonNull(dados.getStatus(), "status");
        entity.publicadoEm = dados.getPublicadoEm();
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        entity.atualizadoEm = Objects.requireNonNull(dados.getAtualizadoEm(), "atualizadoEm");
        return entity;
    }

    public void atualizar(Anuncio dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        if (dados.getVersao() != getVersao()) throw new IllegalArgumentException("Versão desatualizada");
        this.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        this.anuncianteId = Objects.requireNonNull(dados.getAnuncianteId(), "anuncianteId");
        this.titulo = Objects.requireNonNull(dados.getTitulo(), "titulo");
        this.descricao = dados.getDescricao();
        this.tipoPreco = Objects.requireNonNull(dados.getTipoPreco(), "tipoPreco");
        this.precoCentavos = dados.getPrecoCentavos();
        this.aceitaTroca = dados.getAceitaTroca();
        this.cidade = Objects.requireNonNull(dados.getCidade(), "cidade");
        this.uf = Objects.requireNonNull(dados.getUf(), "uf");
        this.status = Objects.requireNonNull(dados.getStatus(), "status");
        this.publicadoEm = dados.getPublicadoEm();
        this.atualizadoEm = Objects.requireNonNull(dados.getAtualizadoEm(), "atualizadoEm");
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getVeiculoId() { return veiculoId; }

    @Override
    public UUID getAnuncianteId() { return anuncianteId; }

    @Override
    public String getTitulo() { return titulo; }

    @Override
    public String getDescricao() { return descricao; }

    @Override
    public TipoPreco getTipoPreco() { return tipoPreco; }

    @Override
    public Long getPrecoCentavos() { return precoCentavos; }

    @Override
    public Boolean getAceitaTroca() { return aceitaTroca; }

    @Override
    public String getCidade() { return cidade; }

    @Override
    public String getUf() { return uf; }

    @Override
    public StatusAnuncio getStatus() { return status; }

    @Override
    public Instant getPublicadoEm() { return publicadoEm; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    @Override
    public Instant getAtualizadoEm() { return atualizadoEm; }

    @Override
    public int getVersao() { return versao == null ? 0 : versao; }
}
