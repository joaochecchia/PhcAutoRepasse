package repasse.phcauto.backend.usuarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.request.EnderecoPatchRequest;

@SpringBootTest(properties = {"app.projection.bootstrap=false", "app.projection.retry-delay=1s"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "USUARIOS_INTEGRATION_TEST", matches = "true")
class UsuarioAggregateIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired @Qualifier("writeDataSource") DataSource write;
    @Autowired @Qualifier("readDataSource") DataSource read;

    @Test void patchPfAlteraSomenteBlocosEnviadosEDeleteRemoveAgregadoNosDoisBancos() throws Exception {
        var criado = criar(TipoPessoa.PF);
        UUID id = criado.id();
        var source = new JdbcTemplate(write);
        String emailAnterior = source.queryForObject(
                "select email from identidade.usuarios where id=?", String.class, id);
        String cpfAnterior = source.queryForObject(
                "select cpf from identidade.usuarios_pf where usuario_id=?", String.class, id).strip();

        var patch = new AtualizarUsuarioRequest(null, null, null, null, null, null, null, null,
                new EnderecoPatchRequest(null, "Campinas", null, null, null, "", null));
        mvc.perform(patch("/api/v1/usuarios/{id}", id).contentType("application/json")
                        .content(json.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(emailAnterior))
                .andExpect(jsonPath("cpf").value(cpfAnterior))
                .andExpect(jsonPath("endereco.cidade").value("Campinas"))
                .andExpect(jsonPath("endereco.complemento").doesNotExist())
                .andExpect(jsonPath("senha").doesNotExist())
                .andExpect(jsonPath("senhaHash").doesNotExist());

        mvc.perform(get("/api/v1/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("tipoPessoa").value("PF"))
                .andExpect(jsonPath("endereco.cidade").value("Campinas"));
        assertEquals(emailAnterior, source.queryForObject(
                "select email from identidade.usuarios where id=?", String.class, id));
        assertEquals(cpfAnterior, source.queryForObject(
                "select cpf from identidade.usuarios_pf where usuario_id=?", String.class, id).strip());

        var projection = new JdbcTemplate(read);
        await(() -> "Campinas".equals(projection.queryForObject(
                "select cidade from identidade.enderecos_usuario where usuario_id=?", String.class, id)));

        mvc.perform(delete("/api/v1/usuarios/{id}", id)).andExpect(status().isNoContent());
        assertAgregadoAusente(source, id, "usuarios_pf");
        await(() -> agregadoAusente(projection, id, "usuarios_pf"));
        mvc.perform(get("/api/v1/usuarios/{id}", id)).andExpect(status().isNotFound());
    }

    @Test void patchPjAtualizaUsuarioPerfilEEnderecoSemCriarPf() throws Exception {
        var criado = criar(TipoPessoa.PJ);
        UUID id = criado.id();
        String novoCnpj = documento(false);
        var patch = new AtualizarUsuarioRequest("Nova Empresa", null, "1133335555", null,
                null, null, novoCnpj, "Nova Razão LTDA",
                new EnderecoPatchRequest("13010000", null, null, "Rua Nova", "20", null, null));

        mvc.perform(patch("/api/v1/usuarios/{id}", id).contentType("application/json")
                        .content(json.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("nome").value("Nova Empresa"))
                .andExpect(jsonPath("cnpj").value(novoCnpj))
                .andExpect(jsonPath("razaoSocial").value("Nova Razão LTDA"))
                .andExpect(jsonPath("cpf").doesNotExist())
                .andExpect(jsonPath("endereco.rua").value("Rua Nova"));

        var source = new JdbcTemplate(write);
        assertEquals("Nova Empresa", source.queryForObject(
                "select nome from identidade.usuarios where id=?", String.class, id));
        assertEquals("Nova Empresa", source.queryForObject(
                "select nome_fantasia from identidade.usuarios_pj where usuario_id=?", String.class, id));
        assertEquals(0, source.queryForObject(
                "select count(*) from identidade.usuarios_pf where usuario_id=?", Integer.class, id));
        mvc.perform(delete("/api/v1/usuarios/{id}", id)).andExpect(status().isNoContent());
    }

    @Test void vinculoDeCompraBloqueiaDeleteEReverteRemocaoDePerfilEEndereco() throws Exception {
        var criado = criar(TipoPessoa.PF);
        UUID id = criado.id();
        var source = new JdbcTemplate(write);
        source.update("insert into identidade.dados_compra_pf(usuario_id, rg) values (?, ?)",
                id, "RG-TESTE");

        mvc.perform(delete("/api/v1/usuarios/{id}", id)).andExpect(status().isConflict());
        assertEquals(1, source.queryForObject(
                "select count(*) from identidade.usuarios where id=?", Integer.class, id));
        assertEquals(1, source.queryForObject(
                "select count(*) from identidade.usuarios_pf where usuario_id=?", Integer.class, id));
        assertEquals(1, source.queryForObject(
                "select count(*) from identidade.enderecos_usuario where usuario_id=?", Integer.class, id));

        source.update("delete from identidade.dados_compra_pf where usuario_id=?", id);
        mvc.perform(delete("/api/v1/usuarios/{id}", id)).andExpect(status().isNoContent());
    }

    @Test void endpointsDiretosDePfPjEEnderecoNaoExistem() throws Exception {
        for (String rota : new String[] {"usuarios-pf", "usuarios-pj", "enderecos"}) {
            mvc.perform(post("/api/v1/identidade/" + rota).contentType("application/json").content("{}"))
                    .andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/identidade/" + rota + "/" + UUID.randomUUID()))
                    .andExpect(status().isNotFound());
            mvc.perform(put("/api/v1/identidade/" + rota + "/" + UUID.randomUUID())
                            .contentType("application/json").content("{}"))
                    .andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/identidade/" + rota + "/" + UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }

    @Test void patchVazioOuCamposDoPerfilErradoSaoRejeitados() throws Exception {
        UUID id = criar(TipoPessoa.PF).id();
        mvc.perform(patch("/api/v1/usuarios/{id}", id).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/usuarios/{id}", id).contentType("application/json")
                        .content("{\"cnpj\":\"11222333000181\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/usuarios/{id}", id)).andExpect(status().isNoContent());
    }

    private UsuarioResponse criar(TipoPessoa tipo) throws Exception {
        String documento = documento(tipo == TipoPessoa.PF);
        var request = new CriarUsuarioRequest(tipo, tipo == TipoPessoa.PF ? "Pessoa Teste" : "Empresa Teste",
                UUID.randomUUID() + "@example.com", "11999999999", "Senha-de-teste-123",
                tipo == TipoPessoa.PF ? documento : null,
                tipo == TipoPessoa.PF ? LocalDate.of(1990, 1, 1) : null,
                tipo == TipoPessoa.PJ ? documento : null,
                tipo == TipoPessoa.PJ ? "Empresa Teste LTDA" : null,
                new EnderecoRequest("01001000", "São Paulo", "Sé", "Praça da Sé", "10", "apto", "SP"));
        var body = json.valueToTree(request);
        ((tools.jackson.databind.node.ObjectNode) body).put("senha", request.senha());
        var result = mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andReturn();
        return json.readValue(result.getResponse().getContentAsString(), UsuarioResponse.class);
    }

    private String documento(boolean cpf) {
        int tamanho = cpf ? 11 : 14;
        var value = new StringBuilder();
        for (int i = 0; i < tamanho - 2; i++) value.append(ThreadLocalRandom.current().nextInt(10));
        for (int fim = tamanho - 2; fim < tamanho; fim++) {
            int soma = 0;
            for (int i = 0; i < fim; i++) {
                soma += (value.charAt(i) - '0') * (cpf ? fim + 1 - i : (fim - 1 - i) % 8 + 2);
            }
            int resto = soma % 11;
            value.append(resto < 2 ? 0 : 11 - resto);
        }
        return value.toString();
    }

    private void assertAgregadoAusente(JdbcTemplate jdbc, UUID id, String perfil) {
        assertTrue(agregadoAusente(jdbc, id, perfil));
    }

    private boolean agregadoAusente(JdbcTemplate jdbc, UUID id, String perfil) {
        return jdbc.queryForObject("select count(*) from identidade.usuarios where id=?", Integer.class, id) == 0
                && jdbc.queryForObject("select count(*) from identidade." + perfil
                        + " where usuario_id=?", Integer.class, id) == 0
                && jdbc.queryForObject("select count(*) from identidade.enderecos_usuario"
                        + " where usuario_id=?", Integer.class, id) == 0;
    }

    private void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) fail("Projeção não convergiu em 20 segundos");
            Thread.sleep(50);
        }
    }
}
