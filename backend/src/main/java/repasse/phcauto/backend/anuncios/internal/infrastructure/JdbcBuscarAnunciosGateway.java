package repasse.phcauto.backend.anuncios.internal.infrastructure;

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
import repasse.phcauto.backend.anuncios.AnuncioBuscaResponse;
import repasse.phcauto.backend.anuncios.internal.core.BuscarAnunciosFiltro;
import repasse.phcauto.backend.anuncios.internal.core.BuscarAnunciosGateway;
import repasse.phcauto.backend.anuncios.internal.core.PaginaAnuncios;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

@Component
public class JdbcBuscarAnunciosGateway implements BuscarAnunciosGateway {
    private static final String FROM = """
            from catalogo.anuncios a
            join catalogo.veiculos v on v.id = a.veiculo_id
            join catalogo.enderecos_anuncio e on e.id = a.endereco_id
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
                   moto.cilindradas, v.tipo_freio,
                   coalesce(car.carroceria, cam.carroceria, pic.carroceria) carroceria
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcBuscarAnunciosGateway(@Qualifier("readDataSource") DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public PaginaAnuncios buscar(BuscarAnunciosFiltro f) {
        var where = new ArrayList<String>();
        var p = new MapSqlParameterSource();
        where.add("a.status = 'PUBLICADO'");
        igualEnum(where, p, "v.tipo", "tipo", f.tipoVeiculo());
        igualTexto(where, p, "e.cidade", "cidade", f.cidade());
        igualTexto(where, p, "e.uf", "uf", f.uf());
        igualTexto(where, p, "v.fabricante", "marca", f.marca());
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

        String clausula = " where " + String.join(" and ", where);
        Long total = jdbc.queryForObject("select count(*) " + FROM + clausula, p, Long.class);
        p.addValue("limite", f.tamanho()).addValue("offset", (long) f.pagina() * f.tamanho());
        var itens = jdbc.query(SELECT + FROM + clausula
                + " order by a.publicado_em desc nulls last, a.id desc limit :limite offset :offset", p,
                JdbcBuscarAnunciosGateway::mapear);
        return new PaginaAnuncios(itens, total == null ? 0 : total, f.pagina(), f.tamanho());
    }

    private static AnuncioBuscaResponse mapear(ResultSet r, int row) throws SQLException {
        return new AnuncioBuscaResponse(r.getObject("anuncio_id", java.util.UUID.class),
                r.getObject("veiculo_id", java.util.UUID.class), r.getObject("anunciante_id", java.util.UUID.class),
                TipoVeiculo.valueOf(r.getString("tipo")), r.getString("fabricante"), r.getString("modelo"),
                inteiro(r, "ano_fabricacao"), inteiro(r, "ano_modelo"), enumOuNulo(CondicaoVeiculo.class, r.getString("condicao")),
                r.getString("titulo"), TipoPreco.valueOf(r.getString("tipo_preco")), r.getObject("preco_centavos", Long.class),
                r.getString("cidade"), r.getString("uf"), TipoPessoa.valueOf(r.getString("tipo_pessoa")),
                r.getString("nome_perfil"), r.getString("cambio"), r.getString("combustivel"), r.getString("motorizacao"),
                r.getString("tipo_direcao"), r.getString("tracao"), r.getObject("ipva_pago", Boolean.class),
                r.getObject("blindado", Boolean.class), inteiro(r, "numero_portas"), r.getBigDecimal("cilindrada_litros"),
                inteiro(r, "cilindradas"), r.getString("tipo_freio"), r.getString("carroceria"));
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
