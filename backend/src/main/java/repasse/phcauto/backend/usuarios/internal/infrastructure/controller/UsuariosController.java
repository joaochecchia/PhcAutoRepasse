package repasse.phcauto.backend.usuarios.internal.infrastructure.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.CriarUsuarioRequest;
import repasse.phcauto.backend.usuarios.UsuarioResponse;
import repasse.phcauto.backend.usuarios.UsuariosFacade;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.response.UsuarioDetalhadoResponse;

@RestController
@RequestMapping({"/api/v1/usuarios", "/api/v1/identidade/usuarios"})
public class UsuariosController {
    private final UsuariosFacade usuarios;

    public UsuariosController(UsuariosFacade usuarios) { this.usuarios = usuarios; }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarios.criar(request));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioDetalhadoResponse> buscar(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(usuarios.buscar(usuarioId));
    }

    @PatchMapping("/{usuarioId}")
    public ResponseEntity<UsuarioDetalhadoResponse> atualizar(@PathVariable UUID usuarioId,
            @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarios.atualizar(usuarioId, request));
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> excluir(@PathVariable UUID usuarioId) {
        usuarios.excluir(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
