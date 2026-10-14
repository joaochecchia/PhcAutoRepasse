package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import repasse.phcauto.backend.usuarios.internal.infrastructure.service.FotoPerfilService;
@RestController
@RequestMapping("/api/v1")
public class FotoPerfilController {
    private final FotoPerfilService fotos;
    public FotoPerfilController(FotoPerfilService fotos) { this.fotos = fotos; }
    @PutMapping(value="/usuarios/{id}/foto", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DONO') or authentication.name == #id.toString()")
    public ResponseEntity<Void> salvar(@PathVariable UUID id, @RequestParam("arquivo") MultipartFile arquivo) {
        fotos.salvar(id, arquivo); return ResponseEntity.noContent().build();
    }
    @GetMapping("/perfis/{id}/foto")
    public ResponseEntity<Resource> carregar(@PathVariable UUID id) {
        var foto = fotos.carregar(id);
        if (foto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.noCache()).body(foto);
    }
}
