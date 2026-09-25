package repasse.phcauto.backend.infra.controller.assinaturas;

import io.swagger.v3.oas.annotations.Hidden;
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
@Hidden
@RestController
@RequestMapping("/api/v1/assinaturas/assinaturas")
@Tag(name = "Assinatura")
public class AssinaturaController {
    private final CrudHttpResponseFactory responses;

    public AssinaturaController(CrudHttpResponseFactory responses) {
        this.responses = responses;
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> cadastrar(
            @RequestBody HashMap<String, Object> body) {
        return responses.criado("Assinatura cadastrada com sucesso.", body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> buscar(@PathVariable UUID id) {
        return responses.sucesso("Assinatura consultada com sucesso.", responses.identificador("id", id));
    }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> listar(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limite) {
        return responses.sucesso("Assinaturas listadas com sucesso.", responses.pagina(offset, limite));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> editar(@PathVariable UUID id,
            @RequestBody HashMap<String, Object> body) {
        return responses.sucesso("Assinatura editada com sucesso.", body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> deletar(@PathVariable UUID id) {
        return responses.sucesso("Assinatura deletada com sucesso.", responses.identificador("id", id));
    }
}

