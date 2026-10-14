package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.internal.core.domain.*;
import repasse.phcauto.backend.usuarios.internal.core.gateway.ConsultarPerfisGateway;

@Repository
public class JdbcConsultarPerfisGateway implements ConsultarPerfisGateway {
    private static final int RAIO_INICIAL_KM = 100;
    private static final int RAIO_AMPLIADO_KM = 200;
    private static final double KM_POR_GRAU = 111.32;
    private static final String DISTANCIA_KM =
            "(6371.0088 * 2 * asin(least(1.0, sqrt("
            + "power(sin(radians(m.latitude - :latitude) / 2), 2) "
            + "+ cos(radians(:latitude)) * cos(radians(m.latitude)) "
            + "* power(sin(radians(m.longitude - :longitude) / 2), 2)))))";
    private final NamedParameterJdbcTemplate jdbc;
    private static final String FROM = """
        from identidade.usuarios u
        left join identidade.usuarios_pj pj on pj.usuario_id = u.id
        left join identidade.enderecos_usuario e on e.usuario_id = u.id
        left join localizacao.municipios m on m.codigo_ibge = e.municipio_codigo_ibge
        """;
    private static final String SELECT = """
        select u.id, case when u.tipo_pessoa = 'PJ' then coalesce(pj.nome_fantasia, u.nome)
            else u.nome end nome, u.tipo_pessoa, e.cidade, e.uf,
            u.foto_perfil_chave is not null possui_foto
        """;
    private static final String ANUNCIOS_PUBLICADOS = """
        (select count(*) from catalogo.anuncios a
          where a.anunciante_id = u.id and a.status = 'PUBLICADO')
        """;
    public JdbcConsultarPerfisGateway(@Qualifier("readDataSource") DataSource source) {
        jdbc = new NamedParameterJdbcTemplate(source);
    }
    public PaginaLojistas buscarLojistas(FiltroLojistas f) {
        var params = new MapSqlParameterSource();
        String where = " where u.ativo = true and u.tipo_pessoa = 'PJ' and pj.usuario_id is not null";
        if (f.nome() != null) {
            where += " and lower(pj.nome_fantasia) like lower(:nome) escape '!'";
            params.addValue("nome", "%" + f.nome().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
        Integer raioAplicado = null;
        String ordem;
        long total;
        if (f.temLocalizacao()) {
            double[] origem = f.latitude() == null ? coordenadasCidade(f.cidade(), f.uf())
                    : new double[] {f.latitude(), f.longitude()};
            double latitude = origem[0];
            double longitude = origem[1];
            params.addValue("latitude", latitude).addValue("longitude", longitude)
                    .addValue("raioKm", RAIO_INICIAL_KM);
            atualizarLimites(params, latitude, longitude, RAIO_INICIAL_KM);
            where += " and m.latitude between :latitudeMin and :latitudeMax"
                    + " and m.longitude between :longitudeMin and :longitudeMax"
                    + " and " + DISTANCIA_KM + " <= :raioKm";
            total = contar(where, params);
            raioAplicado = RAIO_INICIAL_KM;
            if (total == 0) {
                raioAplicado = RAIO_AMPLIADO_KM;
                params.addValue("raioKm", raioAplicado);
                atualizarLimites(params, latitude, longitude, raioAplicado);
                total = contar(where, params);
            }
            ordem = DISTANCIA_KM + ", lower(pj.nome_fantasia), u.id";
        } else {
            total = contar(where, params);
            ordem = ANUNCIOS_PUBLICADOS + " desc, (u.foto_perfil_chave is not null) desc, "
                    + "lower(pj.nome_fantasia), u.id";
        }
        params.addValue("limite", f.tamanho()).addValue("offset", (long) f.pagina() * f.tamanho());
        var itens = jdbc.query(SELECT + FROM + where + " order by " + ordem
                        + " limit :limite offset :offset",
                params, (r, n) -> mapear(r));
        return new PaginaLojistas(itens, total, f.pagina(), f.tamanho(), raioAplicado);
    }
    private long contar(String where, MapSqlParameterSource params) {
        Long total = jdbc.queryForObject("select count(*) " + FROM + where, params, Long.class);
        return total == null ? 0 : total;
    }
    private static void atualizarLimites(MapSqlParameterSource params, double latitude,
            double longitude, int raioKm) {
        double deltaLatitude = raioKm / KM_POR_GRAU;
        double cosseno = Math.max(Math.abs(Math.cos(Math.toRadians(latitude))), 0.01);
        double deltaLongitude = Math.min(180, raioKm / (KM_POR_GRAU * cosseno));
        params.addValue("latitudeMin", latitude - deltaLatitude)
                .addValue("latitudeMax", latitude + deltaLatitude)
                .addValue("longitudeMin", longitude - deltaLongitude)
                .addValue("longitudeMax", longitude + deltaLongitude);
    }
    private double[] coordenadasCidade(String cidade, String uf) {
        var valores = jdbc.query("select latitude, longitude from localizacao.municipios "
                        + "where uf = :uf and nome_normalizado = localizacao.normalizar_nome(:cidade) limit 1",
                Map.of("cidade", cidade, "uf", uf.toUpperCase(Locale.ROOT)),
                (r, n) -> new double[] {r.getDouble(1), r.getDouble(2)});
        if (valores.isEmpty()) throw new IllegalArgumentException("Cidade cadastrada não encontrada no catálogo de municípios");
        return valores.get(0);
    }
    public Optional<PerfilPublico> buscarPerfil(UUID id) {
        return jdbc.query(SELECT + FROM + " where u.id = :id and u.ativo = true", Map.of("id", id),
                (r, n) -> mapear(r)).stream().findFirst();
    }
    private static PerfilPublico mapear(java.sql.ResultSet r) throws java.sql.SQLException {
        return new PerfilPublico(r.getObject("id", UUID.class), r.getString("nome"),
                TipoPessoa.valueOf(r.getString("tipo_pessoa")), r.getString("cidade"), r.getString("uf"), r.getBoolean("possui_foto"));
    }
}
