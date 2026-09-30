package repasse.phcauto.backend.anuncios;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANUNCIOS_INTEGRATION_TEST", matches = "true")
class AnunciosIntegrationTests {
    @Autowired AnunciosFacade anuncios;
    @Autowired @Qualifier("writeDataSource") DataSource writeDataSource;
    @Autowired @Qualifier("projectionDataSource") DataSource projectionDataSource;
    @Autowired @Qualifier("writeTransactionManager") PlatformTransactionManager transactions;
    private JdbcTemplate write;
    private JdbcTemplate read;
    private UUID usuarioId;
    private final List<AnuncioResponse> criados = new ArrayList<>();

    @BeforeEach void preparar() {
        write = new JdbcTemplate(writeDataSource); read = new JdbcTemplate(projectionDataSource);
        usuarioId = UUID.randomUUID();
        Object[] usuario = {usuarioId, "Anunciante teste", usuarioId+"@teste.local", Timestamp.from(Instant.now()), Timestamp.from(Instant.now())};
        String sql = "insert into identidade.usuarios(id,nome,email,tipo_pessoa,papel,ativo,criado_em,atualizado_em) values (?,?,?,'PF','CLIENTE',true,?,?)";
        write.update(sql, usuario);
        read.update(sql, usuario);
    }

    @AfterEach void limpar() {
        for (int i = criados.size() - 1; i >= 0; i--) {
            var a = criados.get(i);
            write.update("delete from catalogo.anuncios where id=?", a.id());
            write.update("delete from catalogo.motores_barco where barco_id=?", a.veiculoId());
            write.update("delete from catalogo.carros where veiculo_id=?", a.veiculoId());
            write.update("delete from catalogo.motos where veiculo_id=?", a.veiculoId());
            write.update("delete from catalogo.caminhoes where veiculo_id=?", a.veiculoId());
            write.update("delete from catalogo.caminhonetes where veiculo_id=?", a.veiculoId());
            write.update("delete from catalogo.barcos where veiculo_id=?", a.veiculoId());
            write.update("delete from catalogo.linha_amarela where veiculo_id=?", a.veiculoId());
            write.update("delete from catalogo.veiculos where id=?", a.veiculoId());
            write.update("delete from catalogo.enderecos_anuncio where id=(select endereco_id from catalogo.anuncios where id=?)", a.id());
        }
        // Endereços são obtidos antes da remoção do anúncio no fluxo real; neste teste isolado removemos órfãos do usuário de teste.
        write.update("delete from catalogo.enderecos_anuncio e where not exists (select 1 from catalogo.anuncios a where a.endereco_id=e.id)");
        write.update("delete from identidade.usuarios where id=?", usuarioId);
        read.update("delete from identidade.usuarios where id=?", usuarioId);
    }

    @Test void rollbackDesfazCriacaoPatchEDeleteDoAgregado() {
        var tx = new TransactionTemplate(transactions);
        var desfeito = tx.execute(status -> {
            var criado = anuncios.criar(request(TipoVeiculo.CARRO));
            status.setRollbackOnly();
            return criado;
        });
        assertThat(write.queryForObject("select count(*) from catalogo.anuncios where id=?",Long.class,desfeito.id())).isZero();
        assertThat(write.queryForObject("select count(*) from catalogo.veiculos where id=?",Long.class,desfeito.veiculoId())).isZero();
        assertThat(write.queryForObject("select count(*) from catalogo.carros where veiculo_id=?",Long.class,desfeito.veiculoId())).isZero();

        var criado = anuncios.criar(request(TipoVeiculo.CARRO));
        criados.add(criado);
        tx.executeWithoutResult(status -> {
            var patch = new AtualizarAnuncioRequest(null,null,null,null,null,null,null,null,null,"Título que será desfeito",null,
                    null,null,null,null,new EnderecoAnuncioPatchRequest(null,"Cidade desfeita",null,null,null,null,null),
                    new AtualizarAnuncioRequest.CarroPatchRequest(999,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
                    null,null,null,null,null);
            anuncios.atualizar(criado.id(), patch);
            status.setRollbackOnly();
        });
        assertThat(write.queryForObject("select titulo from catalogo.anuncios where id=?",String.class,criado.id())).isEqualTo(criado.titulo());
        assertThat(write.queryForObject("select cidade from catalogo.enderecos_anuncio where id=(select endereco_id from catalogo.anuncios where id=?)",String.class,criado.id())).isEqualTo(criado.cidade());
        assertThat(write.queryForObject("select quilometragem from catalogo.carros where veiculo_id=?",Integer.class,criado.veiculoId())).isEqualTo(10);

        tx.executeWithoutResult(status -> { anuncios.excluir(criado.id()); status.setRollbackOnly(); });
        assertThat(write.queryForObject("select count(*) from catalogo.anuncios where id=?",Long.class,criado.id())).isOne();
        assertThat(write.queryForObject("select count(*) from catalogo.veiculos where id=?",Long.class,criado.veiculoId())).isOne();
        assertThat(write.queryForObject("select count(*) from catalogo.carros where veiculo_id=?",Long.class,criado.veiculoId())).isOne();
    }

    @Test void atualizaParcialmenteEnderecoEDetalhesEExcluiAgregado() throws Exception {
        var criado=anuncios.criar(request(TipoVeiculo.CARRO));
        criados.add(criado);
        var patch=new AtualizarAnuncioRequest(null,null,null,null,null,null,null,null,null,"Título alterado",null,
                TipoPreco.SOB_CONSULTA,null,false,null,
                new EnderecoAnuncioPatchRequest(null,"Anápolis",null,null,null,null,null),
                new AtualizarAnuncioRequest.CarroPatchRequest(250,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
                null,null,null,null,null);
        var atualizado=anuncios.atualizar(criado.id(),patch);
        assertThat(atualizado.titulo()).isEqualTo("Título alterado");
        assertThat(atualizado.cidade()).isEqualTo("Anápolis");
        assertThat(atualizado.tipoPreco()).isEqualTo(TipoPreco.SOB_CONSULTA);
        assertThat(atualizado.precoCentavos()).isNull();
        assertThat(((AnuncioResponse.CarroResponse)atualizado.detalhes()).quilometragem()).isEqualTo(250);
        assertThat(write.queryForObject("select cidade from catalogo.enderecos_anuncio where id=(select endereco_id from catalogo.anuncios where id=?)",String.class,criado.id())).isEqualTo("Anápolis");
        anuncios.excluir(criado.id());
        criados.remove(criado);
        assertThat(write.queryForObject("select count(*) from catalogo.anuncios where id=?",Long.class,criado.id())).isZero();
        assertThat(write.queryForObject("select count(*) from catalogo.veiculos where id=?",Long.class,criado.veiculoId())).isZero();
    }

    @Test void buscaPublicadosComFiltrosCombinadosEPaginacao() throws Exception {
        var criado = anuncios.criar(request(TipoVeiculo.CARRO));
        criados.add(criado);
        esperarProjecao(1);
        var filtros = new BuscarAnunciosRequest(TipoVeiculo.CARRO, "goiânia", "go", "fabricante",
                repasse.phcauto.backend.domain.model.identidade.TipoPessoa.PF, "Anunciante",
                50_000L, 150_000L, 2025, 2026, "automatico", "flex", "2.0",
                repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo.USADO, null, null,
                null, null, 4, new java.math.BigDecimal("2.0"), "disco", "suv", 0, 10);
        var pagina = anuncios.buscar(filtros);
        assertThat(pagina.total()).isOne();
        assertThat(pagina.anuncios()).singleElement().satisfies(item -> {
            assertThat(item.anuncioId()).isEqualTo(criado.id());
            assertThat(item.nomePerfil()).isEqualTo("Anunciante teste");
            assertThat(item.cidade()).isEqualTo("Goiânia");
            assertThat(item).extracting(AnuncioBuscaResponse::getClass).isNotNull();
        });
    }

    @Test void persisteEProjetaAnunciosDosSeisTipos() throws Exception {
        for (var tipo : TipoVeiculo.values()) {
            var resposta = anuncios.criar(request(tipo));
            criados.add(resposta);
            assertThat(resposta.tipoVeiculo()).isEqualTo(tipo);
            assertThat(resposta.cidade()).isEqualTo("Goiânia");
            assertThat(resposta.getClass().getRecordComponents()).extracting(c -> c.getName())
                    .doesNotContain("cep", "bairro", "rua", "numero", "complemento", "uf");
            assertThat(write.queryForObject("select count(*) from catalogo.anuncios where id=? and endereco_id is not null", Long.class, resposta.id())).isOne();
            assertThat(write.queryForObject("select count(*) from catalogo."+tabela(tipo)+" where veiculo_id=?", Long.class, resposta.veiculoId())).isOne();
        }
        esperarProjecao(criados.size());
    }

    private void esperarProjecao(int quantidade) throws Exception {
        var limite = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(limite)) {
            Long count = read.queryForObject("select count(*) from catalogo.anuncios where anunciante_id=?", Long.class, usuarioId);
            if (count != null && count == quantidade) return;
            Thread.sleep(100);
        }
        assertThat(read.queryForObject("select count(*) from catalogo.anuncios where anunciante_id=?", Long.class, usuarioId)).isEqualTo((long) quantidade);
    }

    private CriarAnuncioRequest request(TipoVeiculo tipo) {
        var endereco = new EnderecoAnuncioRequest("74000000","Goiânia","Centro","Rua 1","10",null,"GO");
        return new CriarAnuncioRequest(usuarioId,tipo,"Fabricante","Modelo",null,2025,2026,"Preto",null,repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo.USADO,"DISCO",
                "Anúncio "+tipo,null,TipoPreco.FIXO,100_000L,true,true,endereco,
                tipo==TipoVeiculo.CARRO?new CriarAnuncioRequest.CarroRequest(10,"SUV","AUTOMATICO","FLEX",null,"2.0",null,new java.math.BigDecimal("2.0"),4,null,"ABC1D23",false,null,null,null,null):null,
                tipo==TipoVeiculo.MOTO?new CriarAnuncioRequest.MotoRequest(10,160,"STREET",null,null,"MANUAL","GASOLINA","DEF2E34",true,null,null):null,
                tipo==TipoVeiculo.CAMINHAO?new CriarAnuncioRequest.CaminhaoRequest(10,"TOCO","BAU","MANUAL","DIESEL",null,null,2,null,null,null,"GHI3F45",false,null,null):null,
                tipo==TipoVeiculo.CAMINHONETE?new CriarAnuncioRequest.CaminhoneteRequest(10,"DUPLA","PICKUP","AUTOMATICO","DIESEL",null,"2.8",null,new java.math.BigDecimal("2.8"),null,4,"JKL4G56",false,null,null,null,null):null,
                tipo==TipoVeiculo.BARCO?new CriarAnuncioRequest.BarcoRequest(null,null,null,null,null,null,null,List.of()):null,
                tipo==TipoVeiculo.LINHA_AMARELA?new CriarAnuncioRequest.LinhaAmarelaRequest(null,null,null,null,null,null,null):null);
    }

    private String tabela(TipoVeiculo tipo) {
        return switch(tipo) { case CARRO->"carros"; case MOTO->"motos"; case CAMINHAO->"caminhoes";
            case CAMINHONETE->"caminhonetes"; case BARCO->"barcos"; case LINHA_AMARELA->"linha_amarela"; };
    }
}
