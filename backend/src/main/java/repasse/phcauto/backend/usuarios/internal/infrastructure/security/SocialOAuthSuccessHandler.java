package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;
import repasse.phcauto.backend.usuarios.SessoesUsuarios;

@Component
@ConditionalOnProperty(name = "app.security.oauth2.enabled", havingValue = "true")
public class SocialOAuthSuccessHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private final SocialLoginService social;
    private final SocialRegistrationTicketService tickets;
    private final SessoesUsuarios sessoes;
    private final AuthCookieService cookies;
    private final String frontend;

    public SocialOAuthSuccessHandler(SocialLoginService social, SocialRegistrationTicketService tickets,
            SessoesUsuarios sessoes, AuthCookieService cookies,
            @Value("${app.security.oauth2.frontend-url:http://localhost:5173}") String frontend) {
        if (!frontend.matches("https?://[^#]+/?")) throw new IllegalArgumentException("OAUTH2_FRONTEND_URL inválida");
        this.social = social; this.tickets = tickets; this.sessoes = sessoes; this.cookies = cookies;
        this.frontend = frontend.replaceAll("/$", "");
    }

    @Override public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        try {
            var oauth = (OAuth2AuthenticationToken) authentication;
            var provider = provider(oauth.getAuthorizedClientRegistrationId());
            OAuth2User principal = oauth.getPrincipal();
            String subject = texto(principal, "sub");
            String email = texto(principal, "email").toLowerCase(java.util.Locale.ROOT);
            String nome = texto(principal, "name");
            if (!Boolean.TRUE.equals(principal.getAttributes().get("email_verified"))) {
                throw new IllegalArgumentException("E-mail Google não verificado");
            }
            var existente = social.buscar(provider, subject);
            if (existente.isPresent()) {
                var emitida = sessoes.iniciar(existente.get().id(), existente.get().papel());
                response.addHeader(HttpHeaders.SET_COOKIE, cookies.criar(emitida.accessToken(), emitida.accessExpiresIn()));
                response.addHeader(HttpHeaders.SET_COOKIE, cookies.criarRefresh(emitida.refreshToken(), emitida.refreshExpiresIn()));
                encerrarSessaoOAuth(request);
                response.sendRedirect(frontend + "/#inicio?oauth=sucesso");
                return;
            }
            if (social.emailPertenceAConta(email)) {
                falhar(request, response, "conta_existente");
                return;
            }
            String ticket = tickets.criar(provider, subject, email, nome);
            encerrarSessaoOAuth(request);
            response.sendRedirect(frontend + "/#cadastro?social_token=" + codificar(ticket));
        } catch (RuntimeException error) {
            falhar(request, response, "dados_indisponiveis");
        }
    }

    @Override public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception) throws IOException {
        falhar(request, response, "autenticacao_cancelada");
    }

    private void falhar(HttpServletRequest request, HttpServletResponse response, String code) throws IOException {
        encerrarSessaoOAuth(request);
        response.sendRedirect(frontend + "/#entrar?oauth_error=" + codificar(code));
    }
    private static void encerrarSessaoOAuth(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
    }
    private static String texto(OAuth2User user, String atributo) {
        Object value = user.getAttributes().get(atributo);
        if (value == null || value.toString().isBlank()) throw new IllegalArgumentException("Atributo ausente");
        return value.toString().strip();
    }
    private static ProvedorAutenticacao provider(String value) {
        if (!"google".equals(value)) throw new IllegalArgumentException("Provedor inválido");
        return ProvedorAutenticacao.GOOGLE;
    }
    private static String codificar(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
