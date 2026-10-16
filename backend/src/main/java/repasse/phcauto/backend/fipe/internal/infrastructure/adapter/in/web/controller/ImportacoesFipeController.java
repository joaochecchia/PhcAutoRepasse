package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.controller;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import repasse.phcauto.backend.fipe.internal.infrastructure.service.ImportacaoFipeService;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response.ImportacaoFipeResponse;
@RestController @RequestMapping("/api/v1/fipe/importacoes") @PreAuthorize("hasRole('DONO')")
public class ImportacoesFipeController {
    private final ImportacaoFipeService service;
    public ImportacoesFipeController(ImportacaoFipeService service){this.service=service;}
    @PostMapping(consumes="application/x-ndjson")
    public ResponseEntity<ImportacaoFipeResponse> importar(@RequestParam String fonte,@RequestParam LocalDate mes,
            HttpServletRequest request) throws IOException {
        try(var in=request.getInputStream()){return ResponseEntity.accepted().body(response(service.enviar(in,fonte,mes)));}
    }
    @GetMapping("/{id}") public ImportacaoFipeResponse consultar(@PathVariable UUID id){return response(service.consultar(id));}
    @PostMapping("/{id}/retomar") public ResponseEntity<ImportacaoFipeResponse> retomar(@PathVariable UUID id){return ResponseEntity.accepted().body(response(service.retomar(id)));}
    private ImportacaoFipeResponse response(ImportacaoFipeService.Estado e){return new ImportacaoFipeResponse(e.id(),e.status(),e.registros(),e.erro());}
}
