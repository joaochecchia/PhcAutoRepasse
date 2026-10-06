package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtLoginService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.LoginRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.SessaoResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
public class LoginController {
    private final JwtLoginService login;
    private final AuthCookieService cookies;

    public LoginController(JwtLoginService login, AuthCookieService cookies) {
        this.login = login;
        this.cookies = cookies;
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<SessaoResponse> login(@RequestBody LoginRequest request) {
        var resultado = login.execute(request);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .header(HttpHeaders.SET_COOKIE, cookies.criar(resultado.token(), resultado.expiresIn()))
                .body(new SessaoResponse(true, resultado.usuarioId(), resultado.papel(), resultado.expiresIn()));
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @GetMapping("/sessao")
    public SessaoResponse sessao(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return SessaoResponse.anonima();
        }
        String papel = authentication.getAuthorities().stream()
                .map(Object::toString).filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5)).findFirst().orElse("CLIENTE");
        return new SessaoResponse(true, UUID.fromString(authentication.getName()), papel, 0);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.remover())
                .header("Cache-Control", "no-store")
                .build();
    }
}
