package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.gateway;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioResumo;
import repasse.phcauto.backend.anuncios.internal.core.domain.BuscarAnunciosFiltro;
import repasse.phcauto.backend.anuncios.internal.core.domain.PaginaAnuncios;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosPaginaInicialGateway;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

@Component
public class JdbcBuscarAnunciosGateway implements BuscarAnunciosGateway, BuscarAnunciosPaginaInicialGateway {
    private static final int RAIO_INICIAL_KM = 100;
    private static final int RAIO_AMPLIADO_KM = 200;
    private static final double KM_POR_GRAU = 111.32;
    private static final String DISTANCIA_KM = """
            6371.0088 * 2 * asin(least(1.0, sqrt(
                power(sin(radians(mun.latitude - :latitude) / 2), 2)
                + cos(radians(:latitude)) * cos(radians(mun.latitude))
                * power(sin(radians(mun.longitude - :longitude) / 2), 2)
            )))
            """;
    private static final String FROM = """
            from catalogo.anuncios a
            join catalogo.veiculos v on v.id = a.veiculo_id
            join catalogo.enderecos_anuncio e on e.id = a.endereco_id
            join localizacao.municipios mun on mun.codigo_ibge = e.municipio_codigo_ibge
            join identidade.usuarios u on u.id = a.anunciante_id
            left join identidade.usuarios_pj pj on pj.usuario_id = u.id
            left join catalogo.carros car on car.veiculo_id = v.id
            left join catalogo.motos moto on moto.veiculo_id = v.id
            left join catalogo.caminhoes cam on cam.veiculo_id = v.id
            left join catalogo.caminhonetes pic on pic.veiculo_id = v.id
            """;
    private static final String SELECT = """
            select a.id anuncio_id, v.id veiculo_id, a.anunciante_id, v.tipo, v.fabricante,
                   v.modelo, v.ano_fabricacao, v.ano_modelo, v.condicao, a.titulo,
                   a.tipo_preco, a.preco_centavos, e.cidade, e.uf, u.tipo_pessoa,
                   coalesce(nullif(pj.nome_fantasia, ''), u.nome) nome_perfil,
                   coalesce(car.cambio, moto.cambio, cam.cambio, pic.cambio) cambio,
                   coalesce(car.combustivel, moto.combustivel, cam.combustivel, pic.combustivel) combustivel,
                   coalesce(car.motorizacao, pic.motorizacao) motorizacao,
                   coalesce(car.tipo_direcao, cam.tipo_direcao, pic.tipo_direcao) tipo_direcao,
                   coalesce(car.tracao, cam.tracao, pic.tracao) tracao,
                   coalesce(car.ipva_pago, moto.ipva_pago, cam.ipva_pago, pic.ipva_pago) ipva_pago,
                   coalesce(car.blindado, pic.blindado) blindado,
                   coalesce(car.numero_portas, pic.numero_portas) numero_portas,
                   coalesce(car.cilindrada_litros, pic.cilindrada_litros) cilindrada_litros,
                   moto.cilindradas, v.tipo_freio, v.historico_leilao, v.historico_sinistro,
                   coalesce(car.carroceria, cam.carroceria, pic.carroceria) carroceria,
                   %s distancia_km,
                   (select f.id from catalogo.fotos f where f.anuncio_id = a.id
                    order by f.posicao, f.id limit 1) foto_principal_id,
                   (select count(*) from catalogo.fotos f where f.anuncio_id = a.id) foto_quantidade,
                   a.publicado_em
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcBuscarAnunciosGateway(@Qualifier("readDataSource") DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public PaginaAnuncios buscar(BuscarAnunciosFiltro filtro) {
        var where = new ArrayList<String>();
        var parametros = new MapSqlParameterSource();
        filtrosComuns(filtro, where, parametros);

        Integer raio = null;
        Long totalNoRaio = null;
        if (filtro.buscaPorProximidade()) {
            parametros.addValue("latitude", filtro.latitude()).addValue("longitude", filtro.longitude());
            raio = RAIO_INICIAL_KM;
            adicionarRaio(where, parametros, filtro.latitude(), filtro.longitude(), raio);
            totalNoRaio = contar(where, parametros);
            if (totalNoRaio == 0) {
                raio = RAIO_AMPLIADO_KM;
                atualizarRaio(parametros, filtro.latitude(), raio);
                totalNoRaio = contar(where, parametros);
            }
        } else {
            igualTexto(where, parametros, "e.cidade", "cidade", filtro.cidade());
            igualTexto(where, parametros, "e.uf", "uf", filtro.uf());
        }

        long total = totalNoRaio != null ? totalNoRaio : contar(where, parametros);
        long offset = (long) filtro.pagina() * filtro.tamanho();
        parametros.addValue("limite", filtro.tamanho()).addValue("offset", offset);
        String distancia = filtro.buscaPorProximidade() ? DISTANCIA_KM : "null::double precision";
        String ordem = filtro.buscaPorProximidade()
                ? "distancia_km asc, a.publicado_em desc nulls last, a.id desc"
                : "a.publicado_em desc nulls last, a.id desc";
        String clausula = " where " + String.join(" and ", where);
        var itens = jdbc.query(SELECT.formatted(distancia) + FROM + clausula
                + " order by " + ordem + " limit :limite offset :offset", parametros,
                JdbcBuscarAnunciosGateway::mapear);
        boolean temProxima = offset + itens.size() < total;
        return new PaginaAnuncios(itens, total, filtro.pagina(), filtro.tamanho(), raio, temProxima);
    }

    @Override
    public List<AnuncioResumo> buscar(int limite) {
        var parametros = new MapSqlParameterSource("limite", limite);
        String base = SELECT.formatted("null::double precision") + FROM + " where a.status = 'PUBLICADO'";
        String qualidade = """
                (case when fabricante is not null and fabricante <> '' then 1 else 0 end
                 + case when modelo is not null and modelo <> '' then 1 else 0 end
                 + case when ano_fabricacao is not null then 1 else 0 end
                 + case when ano_modelo is not null then 1 else 0 end
                 + case when condicao is not null then 1 else 0 end
                 + case when titulo is not null and titulo <> '' then 1 else 0 end
                 + case when cidade is not null and cidade <> '' and uf is not null then 1 else 0 end
                 + case when cambio is not null and cambio <> '' then 1 else 0 end
                 + case when combustivel is not null and combustivel <> '' then 1 else 0 end
                 + case when motorizacao is not null and motorizacao <> '' then 1 else 0 end
                 + case when carroceria is not null and carroceria <> '' then 1 else 0 end)
                """;
        return jdbc.query("select destaque.* from (" + base + ") destaque order by "
                        + "(foto_principal_id is not null) desc, foto_quantidade desc, "
                        + qualidade + " desc, publicado_em desc nulls last, anuncio_id desc limit :limite",
                parametros, JdbcBuscarAnunciosGateway::mapear);
    }

    private long contar(List<String> where, MapSqlParameterSource parametros) {
        String clausula = " where " + String.join(" and ", where);
        Long total = jdbc.queryForObject("select count(*) " + FROM + clausula, parametros, Long.class);
        return total == null ? 0 : total;
    }

    private static void filtrosComuns(BuscarAnunciosFiltro f, List<String> where,
            MapSqlParameterSource p) {
        where.add("a.status = 'PUBLICADO'");
        igualEnum(where, p, "v.tipo", "tipo", f.tipoVeiculo());
        igualTexto(where, p, "v.fabricante", "marca", f.marca());
        igualTexto(where, p, "v.modelo", "modelo", f.modelo());
        if (texto(f.termo()) != null) {
            where.add("(lower(v.fabricante) like :termo or lower(v.modelo) like :termo "
                    + "or lower(concat(v.fabricante, ' ', v.modelo)) like :termo)");
            p.addValue("termo", "%" + texto(f.termo()).toLowerCase(java.util.Locale.ROOT) + "%");
        }
        igualEnum(where, p, "u.tipo_pessoa", "tipoPessoa", f.tipoPessoa());
        if (texto(f.perfil()) != null) {
            where.add("(lower(u.nome) like :perfil or lower(coalesce(pj.nome_fantasia, '')) like :perfil)");
            p.addValue("perfil", "%" + texto(f.perfil()).toLowerCase() + "%");
        }
        minimo(where, p, "a.preco_centavos", "precoMin", f.precoMinimoCentavos());
        maximo(where, p, "a.preco_centavos", "precoMax", f.precoMaximoCentavos());
        minimo(where, p, "v.ano_modelo", "anoMin", f.anoMinimo());
        maximo(where, p, "v.ano_modelo", "anoMax", f.anoMaximo());
        igualTexto(where, p, "coalesce(car.cambio, moto.cambio, cam.cambio, pic.cambio)", "cambio", f.cambio());
        igualTexto(where, p, "coalesce(car.combustivel, moto.combustivel, cam.combustivel, pic.combustivel)", "combustivel", f.combustivel());
        igualTexto(where, p, "coalesce(car.motorizacao, pic.motorizacao)", "motorizacao", f.motorizacao());
        igualEnum(where, p, "v.condicao", "condicao", f.condicao());
        igualTexto(where, p, "coalesce(car.tipo_direcao, cam.tipo_direcao, pic.tipo_direcao)", "direcao", f.tipoDirecao());
        igualTexto(where, p, "coalesce(car.tracao, cam.tracao, pic.tracao)", "tracao", f.tracao());
        igual(where, p, "coalesce(car.ipva_pago, moto.ipva_pago, cam.ipva_pago, pic.ipva_pago)", "ipva", f.ipvaPago());
        igual(where, p, "coalesce(car.blindado, pic.blindado)", "blindado", f.blindado());
        igual(where, p, "coalesce(car.numero_portas, pic.numero_portas)", "portas", f.numeroPortas());
        igual(where, p, "coalesce(car.cilindrada_litros, pic.cilindrada_litros)", "cilindrada", f.cilindradaLitros());
        igualTexto(where, p, "v.tipo_freio", "freio", f.tipoFreio());
        igualTexto(where, p, "coalesce(car.carroceria, cam.carroceria, pic.carroceria)", "carroceria", f.carroceria());
    }

    private static void adicionarRaio(List<String> where, MapSqlParameterSource p,
            double latitude, double longitude, int raioKm) {
        where.add("mun.latitude is not null and mun.longitude is not null");
        where.add("mun.latitude between :latitudeMin and :latitudeMax");
        where.add("mun.longitude between :longitudeMin and :longitudeMax");
        where.add("(" + DISTANCIA_KM + ") <= :raioKm");
        p.addValue("longitude", longitude);
        atualizarRaio(p, latitude, raioKm);
    }

    private static void atualizarRaio(MapSqlParameterSource p, double latitude, int raioKm) {
        double latitudeDelta = raioKm / KM_POR_GRAU;
        double cosseno = Math.max(Math.abs(Math.cos(Math.toRadians(latitude))), 0.01);
        double longitudeDelta = raioKm / (KM_POR_GRAU * cosseno);
        p.addValue("raioKm", raioKm)
                .addValue("latitudeMin", latitude - latitudeDelta)
                .addValue("latitudeMax", latitude + latitudeDelta)
                .addValue("longitudeMin", p.getValue("longitude") instanceof Number n
                        ? n.doubleValue() - longitudeDelta : -180d)
                .addValue("longitudeMax", p.getValue("longitude") instanceof Number n
                        ? n.doubleValue() + longitudeDelta : 180d);
    }

    private static AnuncioResumo mapear(ResultSet r, int row) throws SQLException {
        Double distancia = r.getObject("distancia_km", Double.class);
        if (distancia != null) distancia = Math.round(distancia * 10.0) / 10.0;
        return new AnuncioResumo(r.getObject("anuncio_id", java.util.UUID.class),
                r.getObject("veiculo_id", java.util.UUID.class), r.getObject("anunciante_id", java.util.UUID.class),
                TipoVeiculo.valueOf(r.getString("tipo")), r.getString("fabricante"), r.getString("modelo"),
                inteiro(r, "ano_fabricacao"), inteiro(r, "ano_modelo"), enumOuNulo(CondicaoVeiculo.class, r.getString("condicao")),
                r.getString("titulo"), TipoPreco.valueOf(r.getString("tipo_preco")), r.getObject("preco_centavos", Long.class),
                r.getString("cidade"), r.getString("uf"), TipoPessoa.valueOf(r.getString("tipo_pessoa")),
                r.getString("nome_perfil"), r.getString("cambio"), r.getString("combustivel"), r.getString("motorizacao"),
                r.getString("tipo_direcao"), r.getString("tracao"), r.getObject("ipva_pago", Boolean.class),
                r.getObject("blindado", Boolean.class), inteiro(r, "numero_portas"), r.getBigDecimal("cilindrada_litros"),
                inteiro(r, "cilindradas"), r.getString("tipo_freio"), r.getString("carroceria"),
                r.getObject("historico_leilao", Boolean.class), r.getObject("historico_sinistro", Boolean.class), distancia,
                r.getObject("foto_principal_id", java.util.UUID.class));
    }

    private static Integer inteiro(ResultSet r, String coluna) throws SQLException { return r.getObject(coluna, Integer.class); }
    private static <E extends Enum<E>> E enumOuNulo(Class<E> tipo, String valor) { return valor == null ? null : Enum.valueOf(tipo, valor); }
    private static String texto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private static void igualTexto(List<String> w, MapSqlParameterSource p, String coluna, String nome, String valor) {
        valor = texto(valor); if (valor != null) { w.add("lower(" + coluna + ") = lower(:" + nome + ")"); p.addValue(nome, valor); }
    }
    private static void igualEnum(List<String> w, MapSqlParameterSource p, String coluna, String nome, Enum<?> valor) {
        if (valor != null) { w.add(coluna + " = :" + nome); p.addValue(nome, valor.name()); }
    }
    private static void igual(List<String> w, MapSqlParameterSource p, String coluna, String nome, Object valor) {
        if (valor != null) { w.add(coluna + " = :" + nome); p.addValue(nome, valor); }
    }
    private static void minimo(List<String> w, MapSqlParameterSource p, String coluna, String nome, Number valor) {
        if (valor != null) { w.add(coluna + " >= :" + nome); p.addValue(nome, valor); }
    }
    private static void maximo(List<String> w, MapSqlParameterSource p, String coluna, String nome, Number valor) {
        if (valor != null) { w.add(coluna + " <= :" + nome); p.addValue(nome, valor); }
    }
}
