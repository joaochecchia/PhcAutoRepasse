package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.controller;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import repasse.phcauto.backend.fipe.internal.infrastructure.service.SincronizacaoFipeService;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.request.SincronizarFipeRequest;
@RestController @RequestMapping("/api/v1/fipe/sincronizacoes") @PreAuthorize("hasRole('DONO')")
public class SincronizacaoFipeController {
    private final SincronizacaoFipeService service;
    public SincronizacaoFipeController(SincronizacaoFipeService service){this.service=service;}
    @PostMapping public ResponseEntity<Map<String,UUID>> agendar(@RequestBody SincronizarFipeRequest request){return ResponseEntity.accepted().body(Map.of("id",service.agendar(request.comando())));}
    @GetMapping("/{id}") public Map<String,Object> consultar(@PathVariable UUID id){return service.estado(id);}
}
