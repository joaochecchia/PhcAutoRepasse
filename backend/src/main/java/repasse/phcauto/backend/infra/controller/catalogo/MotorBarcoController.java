package repasse.phcauto.backend.infra.controller.catalogo;

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
@RequestMapping("/api/v1/catalogo/motores-barco")
@Tag(name = "Motor de barco")
public class MotorBarcoController {
    private final CrudHttpResponseFactory responses;

    public MotorBarcoController(CrudHttpResponseFactory responses) {
        this.responses = responses;
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> cadastrar(
            @RequestBody HashMap<String, Object> body) {
        return responses.criado("Motor de barco cadastrado com sucesso.", body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> buscar(@PathVariable UUID id) {
        return responses.sucesso("Motor de barco consultado com sucesso.", responses.identificador("id", id));
    }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> listar(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limite) {
        return responses.sucesso("Motores de barco listados com sucesso.", responses.pagina(offset, limite));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> editar(@PathVariable UUID id,
            @RequestBody HashMap<String, Object> body) {
        return responses.sucesso("Motor de barco editado com sucesso.", body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> deletar(@PathVariable UUID id) {
        return responses.sucesso("Motor de barco deletado com sucesso.", responses.identificador("id", id));
    }
}

