package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.UsuarioResponse;
import repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.UsuarioDetalhadoResponse;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.RedefinirSenhaRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuariosController {
    private final UsuariosFacade usuarios;
    private final AuthCookieService cookies;

    public UsuariosController(UsuariosFacade usuarios, AuthCookieService cookies) {
        this.usuarios = usuarios;
        this.cookies = cookies;
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @org.springframework.security.access.prepost.PreAuthorize("@registroUsuarioAuthorization.podeRegistrar(#request, authentication)")
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody CriarUsuarioRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarios.criar(request, httpRequest.getRemoteAddr()));
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DONO') or authentication.name == #usuarioId.toString()")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioDetalhadoResponse> buscar(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(usuarios.buscar(usuarioId));
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DONO') or authentication.name == #usuarioId.toString()")
    @PatchMapping("/{usuarioId}")
    public ResponseEntity<UsuarioDetalhadoResponse> atualizar(@PathVariable UUID usuarioId,
            @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarios.atualizar(usuarioId, request));
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DONO') or authentication.name == #usuarioId.toString()")
    @PatchMapping("/{usuarioId}/senha")
    public ResponseEntity<Void> redefinirSenha(@PathVariable UUID usuarioId,
            @Valid @RequestBody RedefinirSenhaRequest request) {
        usuarios.redefinirSenha(usuarioId, request);
        return ResponseEntity.noContent()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookies.remover())
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookies.removerRefresh())
                .header("Cache-Control", "no-store")
                .build();
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DONO') or authentication.name == #usuarioId.toString()")
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> excluir(@PathVariable UUID usuarioId) {
        usuarios.excluir(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
