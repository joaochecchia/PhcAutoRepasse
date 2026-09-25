package repasse.phcauto.backend.infra.controller.identidade;

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
@RequestMapping("/api/v1/identidade/dados-compra-pj")
@Tag(name = "Dados de compra da pessoa jurídica")
public class DadosCompraPjController {
    private final CrudHttpResponseFactory responses;

    public DadosCompraPjController(CrudHttpResponseFactory responses) {
        this.responses = responses;
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> cadastrar(
            @RequestBody HashMap<String, Object> body) {
        return responses.criado("Dados de compra da pessoa jurídica cadastrados com sucesso.", body);
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<HashMap<String, Object>> buscar(@PathVariable UUID usuarioId) {
        return responses.sucesso("Dados de compra da pessoa jurídica consultados com sucesso.", responses.identificador("usuarioId", usuarioId));
    }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> listar(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limite) {
        return responses.sucesso("Dados de compra das pessoas jurídicas listados com sucesso.", responses.pagina(offset, limite));
    }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<HashMap<String, Object>> editar(@PathVariable UUID usuarioId,
            @RequestBody HashMap<String, Object> body) {
        return responses.sucesso("Dados de compra da pessoa jurídica editados com sucesso.", body);
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<HashMap<String, Object>> deletar(@PathVariable UUID usuarioId) {
        return responses.sucesso("Dados de compra da pessoa jurídica deletados com sucesso.", responses.identificador("usuarioId", usuarioId));
    }
}

