package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.FotoAnuncioResponse;
import repasse.phcauto.backend.anuncios.internal.infrastructure.service.FotoAnuncioService;

@Validated
@RestController
@RequestMapping("/api/v1/anuncios/{anuncioId}/fotos")
public class FotosAnuncioController {
    private final FotoAnuncioService fotos;
    public FotosAnuncioController(FotoAnuncioService fotos) { this.fotos = fotos; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.security.access.prepost.PreAuthorize("@anuncioAuthorization.podeAlterar(#anuncioId, authentication)")
    public ResponseEntity<FotoAnuncioResponse> enviar(@PathVariable UUID anuncioId,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam @Min(0) @Max(32767) int posicao,
            @RequestParam(required = false) @Size(max = 180) String textoAlternativo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fotos.salvar(anuncioId, arquivo, posicao, textoAlternativo));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    public List<FotoAnuncioResponse> listar(@PathVariable UUID anuncioId) { return fotos.listar(anuncioId); }

    @GetMapping("/{fotoId}/arquivo")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    public ResponseEntity<org.springframework.core.io.Resource> arquivo(@PathVariable UUID anuncioId, @PathVariable UUID fotoId) {
        var arquivo = fotos.carregar(anuncioId, fotoId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(arquivo.contentType()))
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(30)).cachePublic()).body(arquivo.recurso());
    }
}
