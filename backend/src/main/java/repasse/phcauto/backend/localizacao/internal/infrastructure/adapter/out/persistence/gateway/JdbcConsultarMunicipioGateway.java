package repasse.phcauto.backend.localizacao.internal.infrastructure.adapter.out.persistence.gateway;

import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.localizacao.internal.core.domain.Municipio;
import repasse.phcauto.backend.localizacao.internal.core.gateway.ConsultarMunicipioGateway;

@Component
public class JdbcConsultarMunicipioGateway implements ConsultarMunicipioGateway {
    private static final String COLUNAS = """
            select codigo_ibge, nome, uf, latitude, longitude
              from localizacao.municipios
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcConsultarMunicipioGateway(@Qualifier("readDataSource") DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Optional<Municipio> buscarPorCidadeEUf(String cidade, String uf) {
        return jdbc.query(COLUNAS + """
                 where uf = :uf
                   and nome_normalizado = localizacao.normalizar_nome(:cidade)
                 limit 1
                """, Map.of("cidade", cidade, "uf", uf), (rs, row) -> mapear(rs))
                .stream().findFirst();
    }

    @Override
    public Optional<Municipio> buscarPorCodigoIbge(int codigoIbge) {
        return jdbc.query(COLUNAS + " where codigo_ibge = :codigoIbge",
                Map.of("codigoIbge", codigoIbge), (rs, row) -> mapear(rs)).stream().findFirst();
    }

    private static Municipio mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Municipio(rs.getInt("codigo_ibge"), rs.getString("nome"),
                rs.getString("uf").strip(), rs.getDouble("latitude"), rs.getDouble("longitude"));
    }
}
