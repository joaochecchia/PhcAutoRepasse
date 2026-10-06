package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.EnderecoAnuncio;

/** Localização persistida separadamente e vinculada por catalogo.anuncios.endereco_id. */
@Entity
@Table(name = "enderecos_anuncio", schema = "catalogo")
@Access(AccessType.FIELD)
public class EnderecoAnuncioEntity extends EnderecoAnuncio {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "cep", length = 8)
    private String cep;

    @Column(name = "cidade", nullable = false, length = 120)
    private String cidade;

    @Column(name = "bairro", length = 120)
    private String bairro;

    @Column(name = "rua", length = 200)
    private String rua;

    @Column(name = "numero", length = 20)
    private String numero;

    @Column(name = "complemento", length = 200)
    private String complemento;

    @Column(name = "uf", nullable = false, length = 2, columnDefinition = "char(2)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String uf;

    @Column(name = "municipio_codigo_ibge", nullable = false)
    private Integer municipioCodigoIbge;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected EnderecoAnuncioEntity() { }

    public static EnderecoAnuncioEntity criar(EnderecoAnuncio dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new EnderecoAnuncioEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.aplicar(dados);
        return entity;
    }

    public void atualizar(EnderecoAnuncio dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        aplicar(dados);
    }

    private void aplicar(EnderecoAnuncio dados) {
        cep = opcional(dados.getCep());
        if (cep != null && !cep.matches("[0-9]{8}")) throw new IllegalArgumentException("CEP inválido");
        cidade = obrigatorio(dados.getCidade(), "cidade");
        bairro = opcional(dados.getBairro());
        rua = opcional(dados.getRua());
        numero = opcional(dados.getNumero());
        complemento = opcional(dados.getComplemento());
        uf = obrigatorio(dados.getUf(), "uf").toUpperCase(Locale.ROOT);
        if (!uf.matches("[A-Z]{2}")) throw new IllegalArgumentException("UF inválida");
        municipioCodigoIbge = Objects.requireNonNull(
                dados.getMunicipioCodigoIbge(), "Município obrigatório");
    }

    private static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " obrigatório");
        return valor.strip();
    }

    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    @Override public UUID getId() { return id; }
    @Override public String getCep() { return cep; }
    @Override public String getCidade() { return cidade; }
    @Override public String getBairro() { return bairro; }
    @Override public String getRua() { return rua; }
    @Override public String getNumero() { return numero; }
    @Override public String getComplemento() { return complemento; }
    @Override public String getUf() { return uf; }
    @Override public Integer getMunicipioCodigoIbge() { return municipioCodigoIbge; }
    public Long getLockVersion() { return lockVersion; }
}
