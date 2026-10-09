package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtLoginService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.RefreshTokenService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.LoginRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.SessaoResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
public class LoginController {
    private final JwtLoginService login;
    private final AuthCookieService cookies;
    private final RefreshTokenService refreshTokens;

    public LoginController(JwtLoginService login, AuthCookieService cookies, RefreshTokenService refreshTokens) {
        this.login = login;
        this.cookies = cookies;
        this.refreshTokens = refreshTokens;
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<SessaoResponse> login(@RequestBody LoginRequest request) {
        var resultado = login.execute(request);
        var refresh = refreshTokens.iniciar(resultado.usuarioId());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .header(HttpHeaders.SET_COOKIE, cookies.criar(resultado.token(), resultado.expiresIn()))
                .header(HttpHeaders.SET_COOKIE, cookies.criarRefresh(refresh.token(), refresh.expiresIn()))
                .body(new SessaoResponse(true, resultado.usuarioId(), resultado.papel(), resultado.expiresIn()));
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<SessaoResponse> refresh(HttpServletRequest request) {
        try {
            var rotacionado = refreshTokens.rotacionar(cookie(request, AuthCookieService.REFRESH_COOKIE_NAME));
            var access = login.emitir(rotacionado.usuarioId(), rotacionado.papel());
            return ResponseEntity.ok().header("Cache-Control", "no-store")
                    .header(HttpHeaders.SET_COOKIE, cookies.criar(access.token(), access.expiresIn()))
                    .header(HttpHeaders.SET_COOKIE, cookies.criarRefresh(rotacionado.refresh().token(), rotacionado.refresh().expiresIn()))
                    .body(new SessaoResponse(true, access.usuarioId(), access.papel(), access.expiresIn()));
        } catch (RefreshTokenService.RefreshTokenInvalidoException |
                 RefreshTokenService.RefreshTokenReutilizadoException error) {
            return ResponseEntity.status(401).header("Cache-Control", "no-store")
                    .header(HttpHeaders.SET_COOKIE, cookies.remover())
                    .header(HttpHeaders.SET_COOKIE, cookies.removerRefresh()).build();
        }
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
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        refreshTokens.revogar(cookieOuNull(request, AuthCookieService.REFRESH_COOKIE_NAME));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.remover())
                .header(HttpHeaders.SET_COOKIE, cookies.removerRefresh())
                .header("Cache-Control", "no-store")
                .build();
    }

    private String cookie(HttpServletRequest request, String nome) {
        var valor = cookieOuNull(request, nome);
        if (valor == null) throw new RefreshTokenService.RefreshTokenInvalidoException();
        return valor;
    }

    private String cookieOuNull(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) if (nome.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
