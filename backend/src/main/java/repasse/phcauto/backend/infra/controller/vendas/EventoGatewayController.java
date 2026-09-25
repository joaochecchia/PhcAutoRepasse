package repasse.phcauto.backend.infra.controller.vendas;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repasse.phcauto.backend.infra.controller.support.CrudHttpResponseFactory;

/** CRUD HTTP provisório, ainda sem conexão com casos de uso ou gateways. */
@RestController
@RequestMapping("/api/v1/vendas/eventos-gateway")
@Tag(name = "Evento de gateway")
public class EventoGatewayController {
    private final CrudHttpResponseFactory responses;

    public EventoGatewayController(CrudHttpResponseFactory responses) {
        this.responses = responses;
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> cadastrar(
            @RequestBody HashMap<String, Object> body) {
        return responses.criado("Evento de gateway cadastrado com sucesso.", body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> buscar(@PathVariable Long id) {
        return responses.sucesso("Evento de gateway consultado com sucesso.", responses.identificador("id", id));
    }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> listar(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limite) {
        return responses.sucesso("Eventos de gateway listados com sucesso.", responses.pagina(offset, limite));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> editar(@PathVariable Long id,
            @RequestBody HashMap<String, Object> body) {
        return responses.sucesso("Evento de gateway editado com sucesso.", body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> deletar(@PathVariable Long id) {
        return responses.sucesso("Evento de gateway deletado com sucesso.", responses.identificador("id", id));
    }
}

