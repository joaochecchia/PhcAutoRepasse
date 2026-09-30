package repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.request.AtualizarPlanoRequest;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.request.CriarPlanoRequest;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.response.PlanoListaResponse;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.response.PlanoResponse;
import repasse.phcauto.backend.planos.internal.infrastructure.service.PlanosFacade;
@Validated
@RestController
@RequestMapping("/api/v1/assinaturas/planos")
@Tag(name = "Planos")
public class PlanosController {
    private final PlanosFacade planos;
    public PlanosController(PlanosFacade planos) { this.planos = planos; }

    @PostMapping
    @Operation(summary = "Cria um plano")
    public ResponseEntity<PlanoResponse> criar(@Valid @RequestBody CriarPlanoRequest request) {
        var response = planos.criar(request);
        return ResponseEntity.created(URI.create("/api/v1/assinaturas/planos/" + response.id())).body(response);
    }

    @GetMapping("/{planoId}")
    @Operation(summary = "Consulta um plano")
    public ResponseEntity<PlanoResponse> buscar(@PathVariable UUID planoId) {
        return ResponseEntity.ok(planos.buscar(planoId));
    }

    @GetMapping
    @Operation(summary = "Lista os planos")
    public ResponseEntity<PlanoListaResponse> listar(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limite) {
        return ResponseEntity.ok(planos.listar(offset, limite));
    }

    @PatchMapping("/{planoId}")
    @Operation(summary = "Altera parcialmente um plano")
    public ResponseEntity<PlanoResponse> atualizar(@PathVariable UUID planoId,
            @Valid @RequestBody AtualizarPlanoRequest request) {
        return ResponseEntity.ok(planos.atualizar(planoId, request));
    }

    @DeleteMapping("/{planoId}")
    @Operation(summary = "Exclui um plano")
    public ResponseEntity<Void> excluir(@PathVariable UUID planoId) {
        planos.excluir(planoId);
        return ResponseEntity.noContent().build();
    }
}
