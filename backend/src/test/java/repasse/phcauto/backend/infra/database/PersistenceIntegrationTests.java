package repasse.phcauto.backend.infra.database;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BooleanSupplier;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import repasse.phcauto.backend.infra.database.sync.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import repasse.phcauto.backend.domain.model.identidade.*;
import repasse.phcauto.backend.domain.model.assinaturas.*;
import repasse.phcauto.backend.domain.model.catalogo.*;
import repasse.phcauto.backend.domain.model.vendas.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write.UsuarioWriteRepository;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Answers.RETURNS_DEFAULTS;

@SpringBootTest(properties = {"app.projection.retry-delay=1s", "app.projection.bootstrap=false"})
@EnabledIfEnvironmentVariable(named = "PERSISTENCE_INTEGRATION_TEST", matches = "true")
class PersistenceIntegrationTests {
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private final String runId = UUID.randomUUID().toString();
    private final Map<String, UUID> ids = new HashMap<>();
    @Autowired ApplicationContext context;
    @Autowired @Qualifier("writeDataSource") DataSource writeDataSource;
    @Autowired @Qualifier("readDataSource") DataSource readDataSource;
    @Autowired @Qualifier("writeTransactionManager") PlatformTransactionManager writeTransactions;
    @Autowired UsuarioWriteRepository usuarios;
    @Autowired ApplicationEventPublisher events;
    @Autowired PublicationRecovery recovery;
    @Autowired ProjectionReconciler reconciler;
    @Autowired @Qualifier("projectionDataSource") DataSource projectionDataSource;

    @Test
    void persisteTodosOsModelosEConsultaPelaProjecao() throws Exception {
        var fixtures = fixtures();
        var saved = new ArrayList<Saved>();
        var tx = new TransactionTemplate(writeTransactions);
        try {
            tx.executeWithoutResult(status -> {
                for (var fixture : fixtures) saved.add(save(fixture));
            });
            for (var row : saved) {
                var repository = readRepository(row.fixture().type());
                await(() -> repository.existsById(row.id()));
                Object loaded = repository.findById(row.id()).orElseThrow();
                for (var expected : row.fixture().values().entrySet()) {
                    // ID de evento é gerado por sequência; versão é controlada pelo Hibernate.
                    if (row.fixture().type() == EventoGateway.class && expected.getKey().equals("getId")) continue;
                    Object value = loaded.getClass().getMethod(expected.getKey()).invoke(loaded);
                    assertEquals(expected.getValue(), value, row.fixture().type().getSimpleName() + "." + expected.getKey());
                }
                if (row.fixture().type() == EventoGateway.class) assertTrue(((Long) row.id()) > 0);
            }
            var usuario = usuarios.findById(id("usuarios")).orElseThrow();
            assertNull(usuario.getSenhaHash());
            assertNull(((Usuario) readRepository(Usuario.class).findById(id("usuarios")).orElseThrow()).getSenhaHash());
            var edited = new HashMap<>(fixtures.get(0).values());
            edited.put("getNome", "Nome atualizado na origem");
            usuario.atualizar(model(Usuario.class, edited));
            usuarios.saveAndFlush(usuario);
            await(() -> readRepository(Usuario.class).findById(id("usuarios"))
                    .map(value -> ((Usuario) value).getNome().equals("Nome atualizado na origem")).orElse(false));
            assertEquals(24, saved.stream().map(s -> s.fixture().type()).distinct().count());
            var primary = new JdbcTemplate(writeDataSource);
            var replica = new JdbcTemplate(readDataSource);
            assertFalse(primary.queryForObject("select pg_is_in_recovery()", Boolean.class));
            assertFalse(replica.queryForObject("select pg_is_in_recovery()", Boolean.class));
            assertFalse(replica.queryForObject("select has_table_privilege(current_user, 'identidade.usuarios', 'INSERT')", Boolean.class));
            // O usuário de consulta não ganha escrita ao desativar readOnly no JDBC.
            try (var connection = readDataSource.getConnection()) {
                connection.setReadOnly(false);
                try (var statement = connection.createStatement()) {
                    var failure = assertThrows(java.sql.SQLException.class,
                            () -> statement.executeUpdate("delete from identidade.usuarios where false"));
                    assertEquals("42501", failure.getSQLState());
                }
            }
            assertTrue(Arrays.stream(ReadOnlyRepository.class.getMethods()).noneMatch(m -> m.getName().startsWith("save") || m.getName().startsWith("delete")));
        } finally {
            tx.executeWithoutResult(status -> {
                for (int i = saved.size() - 1; i >= 0; i--) {
                    var row = saved.get(i);
                    writeRepository(row.fixture().type()).deleteById(row.id());
                    writeRepository(row.fixture().type()).flush();
                }
            });
        }
        for (var row : saved) await(() -> !readRepository(row.fixture().type()).existsById(row.id()));
    }

    @Test
    void rejeitaAtualizacaoConcorrente() {
        var values = new HashMap<>(usuarioPj().values());
        values.put("getId", UUID.randomUUID());
        values.put("getEmail", "concorrencia-" + runId);
        var initial = usuarios.saveAndFlush(UsuarioEntity.criar(model(Usuario.class, values)));
        try {
            var first = usuarios.findById(initial.getId()).orElseThrow();
            var stale = usuarios.findById(initial.getId()).orElseThrow();
            values.put("getNome", "Primeira edição");
            first.atualizar(model(Usuario.class, values));
            usuarios.saveAndFlush(first);
            values.put("getNome", "Edição desatualizada");
            stale.atualizar(model(Usuario.class, values));
            assertThrows(ObjectOptimisticLockingFailureException.class, () -> usuarios.saveAndFlush(stale));
        } finally {
            usuarios.deleteById(initial.getId());
        }
    }

    @Test
    void rollbackNaoPersisteEventoNemProjecao() throws Exception {
        var values = new HashMap<>(usuarioPj().values());
        UUID id = UUID.randomUUID();
        values.put("getId", id);
        var write = new JdbcTemplate(writeDataSource);
        var tx = new TransactionTemplate(writeTransactions);
        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status -> {
            usuarios.saveAndFlush(UsuarioEntity.criar(model(Usuario.class, values)));
            assertTrue(write.queryForObject("select count(*) from event_publication where serialized_event like ?",
                    Long.class, "%" + id + "%") > 0);
            assertFalse(readRepository(Usuario.class).existsById(id));
            throw new IllegalStateException("rollback intencional");
        }));
        assertFalse(usuarios.existsById(id));
        assertFalse(readRepository(Usuario.class).existsById(id));
        assertEquals(0L, write.queryForObject("select count(*) from event_publication where serialized_event like ?",
                Long.class, "%" + id + "%"));
    }

    @Test
    void recuperaPublicacaoPersistidaAposFalhaNaProjecao() throws Exception {
        UUID id = UUID.randomUUID();
        var values = new HashMap<>(usuarioPj().values());
        values.put("getId", id);
        var target = new JdbcTemplate(projectionDataSource);
        var write = new JdbcTemplate(writeDataSource);
        String constraint = "test_failure_" + id.toString().replace("-", "");
        target.execute("alter table identidade.usuarios add constraint " + constraint
                + " check (id <> '" + id + "'::uuid) not valid");
        try {
            usuarios.saveAndFlush(UsuarioEntity.criar(model(Usuario.class, values)));
            await(() -> write.queryForObject("select count(*) from event_publication where status = 'FAILED' and serialized_event like ?",
                    Long.class, "%" + id + "%") > 0);
            assertTrue(usuarios.existsById(id));
            assertFalse(readRepository(Usuario.class).existsById(id));
            target.execute("alter table identidade.usuarios drop constraint " + constraint);
            await(() -> {
                recovery.retryFailed();
                return readRepository(Usuario.class).existsById(id);
            });
            await(() -> write.queryForObject("select count(*) from event_publication where serialized_event like ?",
                    Long.class, "%" + id + "%") == 0);
        } finally {
            target.execute("alter table identidade.usuarios drop constraint if exists " + constraint);
            usuarios.deleteById(id);
            await(() -> !readRepository(Usuario.class).existsById(id));
        }
    }

    @Test
    void capturaDirtyCheckingERessubmissaoNaoRestauraEstadoAntigo() throws Exception {
        UUID id = UUID.randomUUID();
        var values = new HashMap<>(usuarioPj().values());
        values.put("getId", id);
        var tx = new TransactionTemplate(writeTransactions);
        var oldEvent = RowChanged.of(ProjectionTable.USUARIOS, List.of(id.toString()));
        try {
            // Sem flush explícito: evento deve participar do commit JPA.
            tx.executeWithoutResult(status -> usuarios.save(UsuarioEntity.criar(model(Usuario.class, values))));
            await(() -> readRepository(Usuario.class).existsById(id));
            values.put("getNome", "Estado mais recente");
            tx.executeWithoutResult(status -> usuarios.findById(id).orElseThrow()
                    .atualizar(model(Usuario.class, values)));
            await(() -> readRepository(Usuario.class).findById(id)
                    .map(row -> ((Usuario) row).getNome().equals("Estado mais recente")).orElse(false));
            // Duplicata/entrega atrasada reconcilia o estado atual, em vez de replay de snapshot.
            tx.executeWithoutResult(status -> events.publishEvent(oldEvent));
            await(() -> new JdbcTemplate(writeDataSource).queryForObject(
                    "select count(*) from event_publication where serialized_event like ?",
                    Long.class, "%" + oldEvent.eventId() + "%") == 0);
            assertEquals("Estado mais recente", ((Usuario) readRepository(Usuario.class).findById(id).orElseThrow()).getNome());
            usuarios.deleteAllByIdInBatch(List.of(id));
            await(() -> !readRepository(Usuario.class).existsById(id));
            tx.executeWithoutResult(status -> events.publishEvent(oldEvent));
            await(() -> new JdbcTemplate(writeDataSource).queryForObject(
                    "select count(*) from event_publication where serialized_event like ?",
                    Long.class, "%" + oldEvent.eventId() + "%") == 0);
            assertFalse(readRepository(Usuario.class).existsById(id));
        } finally {
            usuarios.deleteById(id);
        }
    }

    @Test
    void reconciliacaoReconstroiDadosExistentesERemoveOrfaos() throws Exception {
        UUID id = UUID.randomUUID();
        var values = new HashMap<>(usuarioPj().values());
        values.put("getId", id);
        var target = new JdbcTemplate(projectionDataSource);
        var write = new JdbcTemplate(writeDataSource);
        try {
            usuarios.saveAndFlush(UsuarioEntity.criar(model(Usuario.class, values)));
            await(() -> readRepository(Usuario.class).existsById(id));
            await(() -> write.queryForObject("select count(*) from event_publication where serialized_event like ?",
                    Long.class, "%" + id + "%") == 0);
            target.update("delete from identidade.usuarios where id = ?", id);
            reconciler.reconcile();
            await(() -> readRepository(Usuario.class).existsById(id));
            // SQL direto não publica eventos; a reconciliação explícita corrige essa divergência.
            write.update("delete from identidade.usuarios where id = ?", id);
            assertTrue(readRepository(Usuario.class).existsById(id));
            reconciler.reconcile();
            await(() -> !readRepository(Usuario.class).existsById(id));
        } finally {
            usuarios.deleteById(id);
            target.update("delete from identidade.usuarios where id = ?", id);
        }
    }

    private UUID id(String key) { return ids.computeIfAbsent(key, ignored -> UUID.randomUUID()); }
    private record Fixture(Class<?> type, Map<String, Object> values) { }
    private record Saved(Fixture fixture, Object id) { }
    private Fixture fixture(Class<?> type, Map<String, Object> values) { return new Fixture(type, values); }
    private <T> T model(Class<T> type, Map<String, Object> values) {
        return mock(type, invocation -> values.containsKey(invocation.getMethod().getName())
                ? values.get(invocation.getMethod().getName()) : RETURNS_DEFAULTS.answer(invocation));
    }

    @SuppressWarnings("unchecked")
    private JpaRepository<Object, Object> writeRepository(Class<?> model) {
        String name = model.getSimpleName();
        return (JpaRepository<Object, Object>) context.getBean(Character.toLowerCase(name.charAt(0)) + name.substring(1) + "WriteRepository");
    }
    @SuppressWarnings("unchecked")
    private ReadOnlyRepository<Object, Object> readRepository(Class<?> model) {
        String name = model.getSimpleName();
        return (ReadOnlyRepository<Object, Object>) context.getBean(Character.toLowerCase(name.charAt(0)) + name.substring(1) + "ReadRepository");
    }
    private Saved save(Fixture fixture) {
        try {
            String entityName = fixture.type().getName().replace("domain.model", "infra.database.entity") + "Entity";
            if (Set.of(Usuario.class, UsuarioPf.class, UsuarioPj.class, EnderecoUsuario.class).contains(fixture.type())) {
                entityName = "repasse.phcauto.backend.usuarios.internal.infrastructure.entity." + fixture.type().getSimpleName() + "Entity";
            }
            var entity = Class.forName(entityName).getMethod("criar", fixture.type()).invoke(null, model(fixture.type(), fixture.values()));
            Object saved = writeRepository(fixture.type()).saveAndFlush(entity);
            Object key;
            if (fixture.type() == VeiculoCaracteristica.class) key = saved.getClass().getMethod("getId").invoke(saved);
            else if (fixture.values().containsKey("getId")) key = saved.getClass().getMethod("getId").invoke(saved);
            else if (fixture.values().containsKey("getUsuarioId")) key = fixture.values().get("getUsuarioId");
            else key = fixture.values().get("getVeiculoId");
            return new Saved(fixture, key);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
    private void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + java.time.Duration.ofSeconds(30).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) fail("Dados não chegaram à projeção em 30 segundos");
            Thread.sleep(50);
        }
    }
    private Fixture usuarioPj() {
        return fixture(Usuario.class, Map.ofEntries(
                entry("getId", id("usuario_pj")), entry("getNome", "Usuário PJ"),
                entry("getEmail", "pj-" + runId), entry("getSenhaHash", "hash-de-teste"),
                entry("getTipoPessoa", TipoPessoa.PJ), entry("getPapel", PapelUsuario.CLIENTE),
                entry("getAtivo", true), entry("getCriadoEm", NOW), entry("getAtualizadoEm", NOW)));
    }
    private Fixture veiculoExtra(String key, TipoVeiculo tipo) {
        return fixture(Veiculo.class, Map.ofEntries(
                entry("getId", id(key)), entry("getProprietarioId", id("usuarios")),
                entry("getTipo", tipo), entry("getFabricante", "Fabricante"), entry("getModelo", "Modelo"),
                entry("getCriadoEm", NOW), entry("getAtualizadoEm", NOW)));
    }
    private List<Fixture> fixtures() {
        var fixtures = new ArrayList<Fixture>();
        fixtures.add(fixture(Usuario.class, Map.ofEntries(
                entry("getId", id("usuarios")),
                entry("getNome", "teste-" + runId + "-usuarios"),
                entry("getEmail", "teste-" + runId + "-usuarios"),
                entry("getTelefone", "+5511999999999"),
                entry("getTipoPessoa", TipoPessoa.PF),
                entry("getPapel", PapelUsuario.CLIENTE),
                entry("getAtivo", Boolean.TRUE),
                entry("getCriadoEm", NOW),
                entry("getAtualizadoEm", NOW))));
        fixtures.add(fixture(UsuarioPf.class, Map.ofEntries(
                entry("getUsuarioId", id("usuarios")),
                entry("getCpf", "11111111111"),
                entry("getDataNascimento", LocalDate.of(1990, 1, 1)))));
        fixtures.add(usuarioPj());
        fixtures.add(fixture(UsuarioPj.class, Map.ofEntries(
                entry("getUsuarioId", id("usuario_pj")),
                entry("getCnpj", "11111111111111"),
                entry("getRazaoSocial", "teste"),
                entry("getNomeFantasia", "teste"))));
        fixtures.add(fixture(EnderecoUsuario.class, Map.ofEntries(
                entry("getUsuarioId", id("usuarios")), entry("getCep", "01001000"),
                entry("getCidade", "São Paulo"), entry("getBairro", "Sé"),
                entry("getRua", "Praça da Sé"), entry("getNumero", "10"),
                entry("getComplemento", "Sala 2"), entry("getUf", "SP"))));
        fixtures.add(fixture(EnderecoUsuario.class, Map.ofEntries(
                entry("getUsuarioId", id("usuario_pj")), entry("getCep", "20040002"),
                entry("getCidade", "Rio de Janeiro"), entry("getBairro", "Centro"),
                entry("getRua", "Rua da Assembleia"), entry("getNumero", "20"),
                entry("getUf", "RJ"))));
        fixtures.add(fixture(DadosCompraPf.class, Map.ofEntries(
                entry("getUsuarioId", id("usuarios")), entry("getRg", "123456789"),
                entry("getNomePai", "Pai de teste"), entry("getNomeMae", "Mãe de teste"),
                entry("getNaturalidade", "São Paulo/SP"), entry("getGenero", "Não informado"))));
        fixtures.add(fixture(DadosCompraPj.class, Map.ofEntries(
                entry("getUsuarioId", id("usuario_pj")), entry("getInscricaoEstadual", "ISENTO"),
                entry("getRegimeTributario", "Simples Nacional"))));
        fixtures.add(fixture(IdentidadeExterna.class, Map.ofEntries(
                entry("getId", id("google")), entry("getUsuarioId", id("usuarios")),
                entry("getProvedor", ProvedorAutenticacao.GOOGLE), entry("getIdentificadorExterno", "google-" + runId))));
        fixtures.add(fixture(IdentidadeExterna.class, Map.ofEntries(
                entry("getId", id("facebook")), entry("getUsuarioId", id("usuarios")),
                entry("getProvedor", ProvedorAutenticacao.FACEBOOK), entry("getIdentificadorExterno", "facebook-" + runId))));
        fixtures.add(fixture(Plano.class, Map.ofEntries(
                entry("getId", id("planos")),
                entry("getNome", "teste-" + runId + "-planos"),
                entry("getValorCentavos", 12500L),
                entry("getPeriodoMeses", 2),
                entry("getLimiteAnuncios", 2),
                entry("getAtivo", Boolean.TRUE),
                entry("getCriadoEm", NOW))));
        fixtures.add(fixture(Assinatura.class, Map.ofEntries(
                entry("getId", id("assinaturas")),
                entry("getUsuarioId", id("usuarios")),
                entry("getPlanoId", id("planos")),
                entry("getValorContratadoCentavos", 12500L),
                entry("getStatus", StatusAssinatura.PENDENTE),
                entry("getInicioEm", NOW),
                entry("getFimEm", NOW),
                entry("getCriadoEm", NOW))));
        fixtures.add(fixture(Veiculo.class, Map.ofEntries(
                entry("getId", id("veiculos")),
                entry("getProprietarioId", id("usuarios")),
                entry("getTipo", TipoVeiculo.CARRO),
                entry("getFabricante", "teste"),
                entry("getModelo", "teste"),
                entry("getVersao", "teste"),
                entry("getAnoFabricacao", 2),
                entry("getAnoModelo", 2),
                entry("getCor", "teste"),
                entry("getIdentificadorPublico", "teste"),
                entry("getCriadoEm", NOW),
                entry("getAtualizadoEm", NOW))));
        fixtures.add(fixture(Carro.class, Map.ofEntries(
                entry("getVeiculoId", id("veiculos")),
                entry("getQuilometragem", 2),
                entry("getCarroceria", "teste"),
                entry("getCambio", "teste"),
                entry("getCombustivel", "teste"),
                entry("getTracao", "teste"),
                entry("getMotorizacao", "teste"),
                entry("getNumeroPortas", 2),
                entry("getNumeroLugares", 2),
                entry("getFinalPlaca", "1"),
                entry("getUnicoDono", Boolean.TRUE),
                entry("getIpvaPago", Boolean.TRUE),
                entry("getLicenciado", Boolean.TRUE),
                entry("getBlindado", Boolean.TRUE))));
        fixtures.add(veiculoExtra("motos", TipoVeiculo.MOTO));
        fixtures.add(fixture(Moto.class, Map.ofEntries(
                entry("getVeiculoId", id("motos")),
                entry("getQuilometragem", 2),
                entry("getCilindradas", 2),
                entry("getCategoria", "teste"),
                entry("getPartida", "teste"),
                entry("getRefrigeracao", "teste"),
                entry("getCambio", "teste"),
                entry("getCombustivel", "teste"),
                entry("getFinalPlaca", "1"),
                entry("getIpvaPago", Boolean.TRUE),
                entry("getLicenciado", Boolean.TRUE))));
        fixtures.add(veiculoExtra("caminhoes", TipoVeiculo.CAMINHAO));
        fixtures.add(fixture(Caminhao.class, Map.ofEntries(
                entry("getVeiculoId", id("caminhoes")),
                entry("getQuilometragem", 2),
                entry("getConfiguracao", "teste"),
                entry("getCarroceria", "teste"),
                entry("getCambio", "teste"),
                entry("getCombustivel", "teste"),
                entry("getTracao", "teste"),
                entry("getNumeroEixos", 2),
                entry("getCapacidadeCargaKg", 2),
                entry("getPesoBrutoTotalKg", 2),
                entry("getImplemento", "teste"),
                entry("getFinalPlaca", "1"),
                entry("getIpvaPago", Boolean.TRUE),
                entry("getLicenciado", Boolean.TRUE))));
        fixtures.add(veiculoExtra("caminhonetes", TipoVeiculo.CAMINHONETE));
        fixtures.add(fixture(Caminhonete.class, Map.ofEntries(
                entry("getVeiculoId", id("caminhonetes")),
                entry("getQuilometragem", 2),
                entry("getTipoCabine", "teste"),
                entry("getCarroceria", "teste"),
                entry("getCambio", "teste"),
                entry("getCombustivel", "teste"),
                entry("getTracao", "teste"),
                entry("getMotorizacao", "teste"),
                entry("getCapacidadeCargaKg", 2),
                entry("getNumeroPortas", 2),
                entry("getFinalPlaca", "1"),
                entry("getUnicoDono", Boolean.TRUE),
                entry("getIpvaPago", Boolean.TRUE),
                entry("getLicenciado", Boolean.TRUE))));
        fixtures.add(veiculoExtra("barcos", TipoVeiculo.BARCO));
        fixtures.add(fixture(Barco.class, Map.ofEntries(
                entry("getVeiculoId", id("barcos")),
                entry("getTamanhoPes", new BigDecimal("12.50")),
                entry("getEstilo", "teste"),
                entry("getMaterialCasco", "teste"),
                entry("getCapacidadePessoas", 2),
                entry("getNumeroCabines", 2),
                entry("getHorasUso", 2),
                entry("getRegistroMaritimo", "teste"))));
        fixtures.add(fixture(MotorBarco.class, Map.ofEntries(
                entry("getId", id("motores_barco")),
                entry("getBarcoId", id("barcos")),
                entry("getPosicao", 2),
                entry("getFabricante", "teste"),
                entry("getModelo", "teste"),
                entry("getPotenciaHp", new BigDecimal("12.50")),
                entry("getAno", 2),
                entry("getHorasUso", 2),
                entry("getHorasDesdeRevisao", 2),
                entry("getCombustivel", "teste"))));
        fixtures.add(veiculoExtra("linha_amarela", TipoVeiculo.LINHA_AMARELA));
        fixtures.add(fixture(LinhaAmarela.class, Map.ofEntries(
                entry("getVeiculoId", id("linha_amarela")),
                entry("getTipoMaquina", "teste"),
                entry("getHorimetro", 2),
                entry("getPesoOperacionalKg", 2),
                entry("getPotenciaHp", new BigDecimal("12.50")),
                entry("getTipoEsteiraOuPneu", "teste"),
                entry("getCapacidadeCacambaM3", new BigDecimal("1.250")),
                entry("getNumeroSerie", "teste"))));
        fixtures.add(fixture(Caracteristica.class, Map.ofEntries(
                entry("getId", id("caracteristicas")),
                entry("getTipoVeiculo", TipoVeiculo.CARRO),
                entry("getNome", "teste"),
                entry("getGrupo", "teste"))));
        fixtures.add(fixture(VeiculoCaracteristica.class, Map.ofEntries(
                entry("getVeiculoId", id("veiculos")),
                entry("getCaracteristicaId", id("caracteristicas")),
                entry("getObservacao", "teste"))));
        fixtures.add(fixture(Anuncio.class, Map.ofEntries(
                entry("getId", id("anuncios")),
                entry("getVeiculoId", id("veiculos")),
                entry("getAnuncianteId", id("usuarios")),
                entry("getTitulo", "teste"),
                entry("getDescricao", "teste"),
                entry("getTipoPreco", TipoPreco.FIXO),
                entry("getPrecoCentavos", 12500L),
                entry("getAceitaTroca", Boolean.TRUE),
                entry("getCidade", "teste"),
                entry("getUf", "11"),
                entry("getStatus", StatusAnuncio.RASCUNHO),
                entry("getPublicadoEm", NOW),
                entry("getCriadoEm", NOW),
                entry("getAtualizadoEm", NOW),
                entry("getVersao", 0))));
        fixtures.add(fixture(Foto.class, Map.ofEntries(
                entry("getId", id("fotos")),
                entry("getAnuncioId", id("anuncios")),
                entry("getChaveArquivo", "teste-" + runId + "-fotos"),
                entry("getPosicao", 2),
                entry("getTextoAlternativo", "teste"))));
        fixtures.add(fixture(Compra.class, Map.ofEntries(
                entry("getId", id("compras")),
                entry("getAnuncioId", id("anuncios")),
                entry("getCompradorId", id("usuario_pj")),
                entry("getVendedorId", id("usuarios")),
                entry("getValorCentavos", 12500L),
                entry("getTituloVeiculoSnapshot", "teste"),
                entry("getStatus", StatusCompra.AGUARDANDO_PAGAMENTO),
                entry("getCriadoEm", NOW),
                entry("getPagoEm", NOW))));
        fixtures.add(fixture(Pagamento.class, Map.ofEntries(
                entry("getId", id("pagamentos")),
                entry("getCompraId", id("compras")),
                entry("getProvedor", "teste"),
                entry("getReferenciaExterna", "teste"),
                entry("getMetodo", MetodoPagamento.PIX),
                entry("getStatus", StatusPagamento.PENDENTE),
                entry("getValorCentavos", 12500L),
                entry("getVenceEm", NOW),
                entry("getConfirmadoEm", NOW),
                entry("getCriadoEm", NOW))));
        fixtures.add(fixture(EventoGateway.class, Map.ofEntries(
                entry("getId", 0L),
                entry("getProvedor", "teste"),
                entry("getEventoExternoId", "teste"),
                entry("getPagamentoId", id("pagamentos")),
                entry("getRecebidoEm", NOW),
                entry("getProcessadoEm", NOW))));
        return fixtures;
    }
}
