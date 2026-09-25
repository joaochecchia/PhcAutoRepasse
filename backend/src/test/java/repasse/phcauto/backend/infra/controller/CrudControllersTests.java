package repasse.phcauto.backend.infra.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repasse.phcauto.backend.infra.controller.support.CrudHttpResponseFactory;

class CrudControllersTests {
    private static final List<String> CONTROLLERS = List.of(
            "identidade.DadosCompraPfController",
            "identidade.DadosCompraPjController",
            "identidade.IdentidadeExternaController",
            "assinaturas.PlanoController",
            "assinaturas.AssinaturaController",
            "catalogo.VeiculoController",
            "catalogo.CarroController",
            "catalogo.MotoController",
            "catalogo.CaminhaoController",
            "catalogo.CaminhoneteController",
            "catalogo.BarcoController",
            "catalogo.LinhaAmarelaController",
            "catalogo.MotorBarcoController",
            "catalogo.CaracteristicaController",
            "catalogo.VeiculoCaracteristicaController",
            "catalogo.AnuncioController",
            "catalogo.FotoController",
            "vendas.CompraController",
            "vendas.PagamentoController",
            "vendas.EventoGatewayController");

    @Test
    void todosOsModelosPersistentesPossuemControllerCrudProvisorio() throws Exception {
        assertEquals(20, CONTROLLERS.size());
        for (var nome : CONTROLLERS) {
            var type = Class.forName("repasse.phcauto.backend.infra.controller." + nome);
            assertNotNull(type.getAnnotation(RestController.class), nome);
            assertNotNull(type.getAnnotation(RequestMapping.class), nome);
            var methods = Arrays.asList(type.getDeclaredMethods());
            assertEquals(1, methods.stream().filter(m -> m.isAnnotationPresent(PostMapping.class)).count(), nome);
            assertEquals(2, methods.stream().filter(m -> m.isAnnotationPresent(GetMapping.class)).count(), nome);
            assertEquals(1, methods.stream().filter(m -> m.isAnnotationPresent(PutMapping.class)).count(), nome);
            assertEquals(1, methods.stream().filter(m -> m.isAnnotationPresent(DeleteMapping.class)).count(), nome);
            assertTrue(Arrays.stream(type.getDeclaredFields())
                    .allMatch(field -> field.getType().equals(CrudHttpResponseFactory.class)), nome);
        }
    }

    @Test
    void envelopeSegueContratoESanitizaCredenciais() {
        var factory = new CrudHttpResponseFactory();
        var request = new HashMap<String, Object>();
        request.put("nome", "Cliente");
        request.put("senha", "segredo");
        request.put("senhaHash", "hash");
        var nested = new HashMap<String, Object>();
        nested.put("accessToken", "token");
        nested.put("cidade", "São Paulo");
        request.put("endereco", nested);

        var response = factory.criado("Usuário cadastrado com sucesso.", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuário cadastrado com sucesso.", response.getBody().get("menssage"));
        @SuppressWarnings("unchecked")
        var body = (HashMap<String, Object>) response.getBody().get("Body");
        assertEquals("Cliente", body.get("nome"));
        assertFalse(body.containsKey("senha"));
        assertFalse(body.containsKey("senhaHash"));
        @SuppressWarnings("unchecked")
        var endereco = (HashMap<String, Object>) body.get("endereco");
        assertFalse(endereco.containsKey("accessToken"));
        assertEquals("São Paulo", endereco.get("cidade"));
    }
}
