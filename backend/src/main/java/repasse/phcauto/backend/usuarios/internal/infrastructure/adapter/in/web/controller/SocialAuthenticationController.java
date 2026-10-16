package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.*;

@RestController
@RequestMapping("/api/v1/autenticacao/social")
public class SocialAuthenticationController {
    private final SocialRegistrationTicketService tickets;
    private final SocialLoginService social;
    private final AuthCookieService cookies;

    public SocialAuthenticationController(SocialRegistrationTicketService tickets,
            SocialLoginService social, AuthCookieService cookies) {
        this.tickets = tickets; this.social = social; this.cookies = cookies;
    }

    @GetMapping("/cadastro/{token}")
    public CadastroSocialResponse consultar(@PathVariable @Size(max = 100) String token) {
        var dados = tickets.buscar(token);
        return new CadastroSocialResponse(dados.provedor().name(), dados.nome(), dados.email());
    }

    @PostMapping("/cadastro")
    public ResponseEntity<LoginSocialResponse> cadastrar(@Valid @RequestBody CadastroSocialRequest request,
            HttpServletRequest http) {
        var concluido = social.concluirCadastro(request.token(), request.cadastro(), http.getRemoteAddr());
        var sessao = concluido.sessao();
        return ResponseEntity.status(HttpStatus.CREATED).header("Cache-Control", "no-store")
                .header(HttpHeaders.SET_COOKIE, cookies.criar(sessao.accessToken(), sessao.accessExpiresIn()))
                .header(HttpHeaders.SET_COOKIE, cookies.criarRefresh(sessao.refreshToken(), sessao.refreshExpiresIn()))
                .body(new LoginSocialResponse("AUTENTICADO", concluido.usuario().id(),
                        concluido.usuario().papel(), sessao.accessExpiresIn()));
    }

    public record CadastroSocialRequest(@NotBlank @Size(max = 100) String token,
            @NotNull CriarUsuarioRequest cadastro) { }
    public record CadastroSocialResponse(String provedor, String nome, String email) { }
    public record LoginSocialResponse(String status, java.util.UUID usuarioId, String papel, long expiresIn) { }
}
