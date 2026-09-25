package repasse.phcauto.backend.infra.controller.catalogo;

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
@RequestMapping("/api/v1/catalogo/barcos")
@Tag(name = "Barco")
public class BarcoController {
    private final CrudHttpResponseFactory responses;

    public BarcoController(CrudHttpResponseFactory responses) {
        this.responses = responses;
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> cadastrar(
            @RequestBody HashMap<String, Object> body) {
        return responses.criado("Barco cadastrado com sucesso.", body);
    }

    @GetMapping("/{veiculoId}")
    public ResponseEntity<HashMap<String, Object>> buscar(@PathVariable UUID veiculoId) {
        return responses.sucesso("Barco consultado com sucesso.", responses.identificador("veiculoId", veiculoId));
    }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> listar(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limite) {
        return responses.sucesso("Barcos listados com sucesso.", responses.pagina(offset, limite));
    }

    @PutMapping("/{veiculoId}")
    public ResponseEntity<HashMap<String, Object>> editar(@PathVariable UUID veiculoId,
            @RequestBody HashMap<String, Object> body) {
        return responses.sucesso("Barco editado com sucesso.", body);
    }

    @DeleteMapping("/{veiculoId}")
    public ResponseEntity<HashMap<String, Object>> deletar(@PathVariable UUID veiculoId) {
        return responses.sucesso("Barco deletado com sucesso.", responses.identificador("veiculoId", veiculoId));
    }
}

