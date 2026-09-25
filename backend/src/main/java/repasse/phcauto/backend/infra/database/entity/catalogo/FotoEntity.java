package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.Foto;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "fotos", schema = "catalogo")
@Access(AccessType.FIELD)
public class FotoEntity extends Foto {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "anuncio_id", nullable = false)
    private UUID anuncioId;

    @Column(name = "chave_arquivo", nullable = false, columnDefinition = "text")
    private String chaveArquivo;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "posicao", nullable = false)
    private int posicao;

    @Column(name = "texto_alternativo", nullable = true, length = 180)
    private String textoAlternativo;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected FotoEntity() { }

    public static FotoEntity criar(Foto dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new FotoEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.anuncioId = Objects.requireNonNull(dados.getAnuncioId(), "anuncioId");
        entity.chaveArquivo = Objects.requireNonNull(dados.getChaveArquivo(), "chaveArquivo");
        entity.posicao = dados.getPosicao();
        entity.textoAlternativo = dados.getTextoAlternativo();
        return entity;
    }

    public void atualizar(Foto dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.anuncioId = Objects.requireNonNull(dados.getAnuncioId(), "anuncioId");
        this.chaveArquivo = Objects.requireNonNull(dados.getChaveArquivo(), "chaveArquivo");
        this.posicao = dados.getPosicao();
        this.textoAlternativo = dados.getTextoAlternativo();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getAnuncioId() { return anuncioId; }

    @Override
    public String getChaveArquivo() { return chaveArquivo; }

    @Override
    public int getPosicao() { return posicao; }

    @Override
    public String getTextoAlternativo() { return textoAlternativo; }

    public Long getLockVersion() { return lockVersion; }
}
