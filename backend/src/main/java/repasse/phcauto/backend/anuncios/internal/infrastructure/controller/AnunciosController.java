package repasse.phcauto.backend.anuncios.internal.infrastructure.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.anuncios.*;

@RestController
@RequestMapping("/api/v1/anuncios")
public class AnunciosController {
    private final AnunciosFacade anuncios;
    public AnunciosController(AnunciosFacade anuncios) { this.anuncios = anuncios; }

    @PostMapping
    public ResponseEntity<AnuncioResponse> criar(@Valid @RequestBody CriarAnuncioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anuncios.criar(request));
    }

    @PatchMapping("/{anuncioId}")
    public ResponseEntity<AnuncioResponse> atualizar(@PathVariable UUID anuncioId,
            @Valid @RequestBody AtualizarAnuncioRequest request) {
        return ResponseEntity.ok(anuncios.atualizar(anuncioId, request));
    }

    @DeleteMapping("/{anuncioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID anuncioId) {
        anuncios.excluir(anuncioId);
    }
}
