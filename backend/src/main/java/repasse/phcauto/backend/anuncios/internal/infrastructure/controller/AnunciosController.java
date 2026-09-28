package repasse.phcauto.backend.anuncios.internal.infrastructure.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import java.util.HashMap;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.anuncios.*;

@RestController
@RequestMapping("/api/v1/anuncios")
public class AnunciosController {
    private final AnunciosFacade anuncios;
    public AnunciosController(AnunciosFacade anuncios) { this.anuncios = anuncios; }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> buscar(
            @Valid @ParameterObject BuscarAnunciosRequest filtros) {
        var pagina = anuncios.buscar(filtros);
        var resposta = new HashMap<String, Object>();
        resposta.put("mensagem", "Anúncios encontrados com sucesso");
        resposta.put("carros", pagina.anuncios());
        resposta.put("total", pagina.total());
        resposta.put("pagina", pagina.pagina());
        resposta.put("tamanho", pagina.tamanho());
        return ResponseEntity.ok(resposta);
    }

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
