package repasse.phcauto.backend.usuarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.internal.core.CadastroDuplicadoException;

@SpringBootTest(properties = {"app.projection.bootstrap=false", "app.projection.retry-delay=1s"})
@AutoConfigureMockMvc
@Import(UsuariosIntegrationTests.EventConfig.class)
@EnabledIfEnvironmentVariable(named = "USUARIOS_INTEGRATION_TEST", matches = "true")
class UsuariosIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UsuariosFacade facade;
    @Autowired JsonMapper json;
    @Autowired @Qualifier("writeDataSource") DataSource write;
    @Autowired @Qualifier("readDataSource") DataSource read;
    @Autowired @Qualifier("writeTransactionManager") PlatformTransactionManager transactions;
    @Autowired Receptor receptor;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write.EnderecoUsuarioWriteRepository enderecos;

    @TestConfiguration(proxyBeanMethods = false)
    static class EventConfig {
        @Bean Receptor receptorCadastro() { return new Receptor(); }
    }
    static class Receptor {
        final ConcurrentMap<UUID, UsuarioCriado> recebidos = new ConcurrentHashMap<>();
        final java.util.Set<UUID> falhas = ConcurrentHashMap.newKeySet();
        final ConcurrentMap<UUID, Integer> tentativas = new ConcurrentHashMap<>();
        public boolean recebeu(UUID id) { return recebidos.containsKey(id); }
        public UsuarioCriado evento(UUID id) { return recebidos.get(id); }
        public void falharUmaVez(UUID id) { falhas.add(id); }
        public int tentativas(UUID id) { return tentativas.getOrDefault(id, 0); }
        @ApplicationModuleListener(id = "teste-usuario-criado-v1")
        public void receber(UsuarioCriado evento) {
            tentativas.merge(evento.usuarioId(), 1, Integer::sum);
            if (falhas.remove(evento.usuarioId())) throw new IllegalStateException("Falha transitória simulada");
            recebidos.put(evento.usuarioId(), evento);
        }
    }

    private CriarUsuarioRequest request(TipoPessoa tipo, String documento) {
        return new CriarUsuarioRequest(tipo, "Cliente teste", UUID.randomUUID()+"@example.com", "11999999999",
                "Senha-de-teste-123", tipo == TipoPessoa.PF ? documento : null,
                tipo == TipoPessoa.PF ? LocalDate.of(1990, 1, 1) : null,
                tipo == TipoPessoa.PJ ? documento : null, tipo == TipoPessoa.PJ ? "Empresa teste LTDA" : null,
                new EnderecoRequest("01001000", "São Paulo", "Sé", "Praça da Sé", "10", null, "SP"));
    }

    @Test void criaPfEPjPeloHttpComPerfilEnderecoHashEProjecao() throws Exception {
        for (var tipo : TipoPessoa.values()) {
            var request = request(tipo, documento(tipo));
            var result = mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                    .content(payload(request)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("senha").doesNotExist())
                    .andExpect(jsonPath("senhaHash").doesNotExist()).andReturn();
            var loginBody = json.createObjectNode().put("email", request.email().toUpperCase(java.util.Locale.ROOT))
                    .put("senha", request.senha());
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json").content(json.writeValueAsString(loginBody)))
                    .andExpect(status().isOk()).andExpect(jsonPath("mensagem").value("Login realizado com sucesso"))
                    .andExpect(jsonPath("$.length()").value(1));
            loginBody.put("senha", "senha-incorreta");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json").content(json.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized());
            UUID id = UUID.fromString(json.readTree(result.getResponse().getContentAsString()).get("id").asText());
            var source = new JdbcTemplate(write);
            String hash = source.queryForObject("select senha_hash from identidade.usuarios where id=?", String.class, id);
            assertNotEquals(request.senha(), hash);
            assertTrue(PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(request.senha(), hash));
            String perfil = tipo == TipoPessoa.PF ? "usuarios_pf" : "usuarios_pj";
            String outro = tipo == TipoPessoa.PF ? "usuarios_pj" : "usuarios_pf";
            assertEquals(1, source.queryForObject("select count(*) from identidade."+perfil+" where usuario_id=?", Integer.class, id));
            assertEquals(0, source.queryForObject("select count(*) from identidade."+outro+" where usuario_id=?", Integer.class, id));
            assertEquals(1, source.queryForObject("select count(*) from identidade.enderecos_usuario where usuario_id=?", Integer.class, id));
            var projection = new JdbcTemplate(read);
            await(() -> projection.queryForObject("select count(*) from identidade.usuarios u join identidade."+perfil+
                    " p on p.usuario_id=u.id join identidade.enderecos_usuario e on e.usuario_id=u.id where u.id=?", Integer.class, id) == 1);
            await(() -> receptor.recebeu(id));
            assertEquals(tipo, receptor.evento(id).tipoPessoa());
            mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(payload(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Test void rollbackExternoDesfazTodasAsLinhasEPublicacoes() throws Exception {
        var request = request(TipoPessoa.PF, "11144477735");
        var tx = new TransactionTemplate(transactions);
        UUID id = tx.execute(status -> {
            var resultado = facade.criar(request);
            assertFalse(receptor.recebeu(resultado.id()));
            status.setRollbackOnly();
            return resultado.id();
        });
        var source = new JdbcTemplate(write);
        assertEquals(0, source.queryForObject("select count(*) from identidade.usuarios where id=?", Integer.class, id));
        assertEquals(0, source.queryForObject("select count(*) from identidade.usuarios_pf where usuario_id=?", Integer.class, id));
        assertEquals(0, source.queryForObject("select count(*) from identidade.enderecos_usuario where usuario_id=?", Integer.class, id));
        assertEquals(0, source.queryForObject("select count(*) from event_publication where serialized_event like ?", Integer.class, "%"+id+"%"));
        assertFalse(receptor.recebeu(id));
    }

    @Test void requisicaoInvalidaNaoPersisteNemExpoeSenha() throws Exception {
        var request = request(TipoPessoa.PF, "11111111111");
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(payload(request)))
                .andExpect(status().isBadRequest()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(request.senha()))));
        assertEquals(0, new JdbcTemplate(write).queryForObject("select count(*) from identidade.usuarios where email=?", Integer.class, request.email()));
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test void duasCriacoesConcorrentesDoMesmoCadastroProduzemUmaUnicaConta() throws Exception {
        var request = request(TipoPessoa.PF, documento(TipoPessoa.PF));
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        Callable<Boolean> task = () -> {
            start.await();
            try { facade.criar(request); return true; }
            catch (CadastroDuplicadoException expected) { return false; }
        };
        try {
            var first = pool.submit(task);
            var second = pool.submit(task);
            start.countDown();
            assertNotEquals(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
            assertEquals(1, new JdbcTemplate(write).queryForObject("select count(*) from identidade.usuarios where email=?", Integer.class, request.email()));
        } finally { pool.shutdownNow(); }
    }

    @Test void falhaNoEnderecoReverteUsuarioPerfilEEventos() {
        var request = request(TipoPessoa.PF, documento(TipoPessoa.PF));
        var id = new java.util.concurrent.atomic.AtomicReference<UUID>();
        org.mockito.Mockito.doAnswer(invocation -> {
            repasse.phcauto.backend.usuarios.internal.infrastructure.entity.EnderecoUsuarioEntity entity = invocation.getArgument(0);
            id.set(entity.getUsuarioId());
            throw new IllegalStateException("Falha de endereço simulada");
        }).when(enderecos).saveAndFlush(org.mockito.ArgumentMatchers.any());
        assertThrows(IllegalStateException.class, () -> facade.criar(request));
        assertNotNull(id.get());
        var source = new JdbcTemplate(write);
        assertEquals(0, source.queryForObject("select count(*) from identidade.usuarios where id=?", Integer.class, id.get()));
        assertEquals(0, source.queryForObject("select count(*) from identidade.usuarios_pf where usuario_id=?", Integer.class, id.get()));
        assertEquals(0, source.queryForObject("select count(*) from event_publication where serialized_event like ?", Integer.class, "%"+id.get()+"%"));
        assertFalse(receptor.recebeu(id.get()));
    }

    @Test void recuperaEntregaPersistidaAposFalhaDoListener() throws Exception {
        var tx = new TransactionTemplate(transactions);
        UUID id = tx.execute(status -> {
            var response = facade.criar(request(TipoPessoa.PF, documento(TipoPessoa.PF)));
            receptor.falharUmaVez(response.id());
            return response.id();
        });
        await(() -> receptor.recebeu(id));
        assertEquals(2, receptor.tentativas(id));
        var jdbc = new JdbcTemplate(write);
        await(() -> jdbc.queryForObject("select count(*) from event_publication where serialized_event like ?",
                Integer.class, "%"+id+"%") == 0);
    }

    private String documento(TipoPessoa tipo) {
        boolean cpf = tipo == TipoPessoa.PF;
        int tamanho = cpf ? 11 : 14;
        var value = new StringBuilder();
        for (int i = 0; i < tamanho - 2; i++) value.append(ThreadLocalRandom.current().nextInt(10));
        for (int fim = tamanho - 2; fim < tamanho; fim++) {
            int soma = 0;
            for (int i = 0; i < fim; i++) soma += (value.charAt(i)-'0') * (cpf ? fim+1-i : (fim-1-i)%8+2);
            int resto = soma % 11;
            value.append(resto < 2 ? 0 : 11-resto);
        }
        return value.toString();
    }

    private String payload(CriarUsuarioRequest r) {
        var node = json.valueToTree(r);
        ((tools.jackson.databind.node.ObjectNode) node).put("senha", r.senha());
        return json.writeValueAsString(node);
    }
    private void await(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) fail("Evento/projeção não chegou em 20 segundos");
            Thread.sleep(50);
        }
    }
}
