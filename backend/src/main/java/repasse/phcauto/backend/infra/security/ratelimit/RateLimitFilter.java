package repasse.phcauto.backend.infra.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiter limiter;
    private final RateLimitSettings settings;
    public RateLimitFilter(RateLimiter limiter, RateLimitSettings settings) {
        this.limiter = limiter; this.settings = settings;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var regra = regra(request);
        if (regra == null) { chain.doFilter(request, response); return; }
        try {
            limiter.consumir(regra.nome(), identificador(request, regra.porUsuario()), regra.limite(), regra.janela());
            chain.doFilter(request, response);
        } catch (RateLimitExceededException error) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(error.retryAfterSeconds()));
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"title\":\"Muitas requisições\","
                    + "\"detail\":\"Aguarde antes de tentar novamente\"}");
        }
    }

    private RegraHttp regra(HttpServletRequest request) {
        String metodo = request.getMethod();
        String caminho = request.getRequestURI();
        if ("POST".equals(metodo) && caminho.equals("/api/v1/usuarios/login"))
            return regra("login", 10, Duration.ofMinutes(1), false);
        if ("POST".equals(metodo) && caminho.equals("/api/v1/usuarios/registrar"))
            return regra("registro", 5, Duration.ofHours(1), false);
        if ("POST".equals(metodo) && caminho.equals("/api/v1/usuarios/refresh"))
            return regra("refresh", 20, Duration.ofMinutes(1), false);
        if ("GET".equals(metodo) && (caminho.equals("/api/v1/anuncios")
                || caminho.equals("/api/v1/anuncios/pagina-inicial")
                || caminho.equals("/api/v1/lojistas")
                || caminho.equals("/api/v1/localizacao/municipios/coordenadas")))
            return regra("busca", 120, Duration.ofMinutes(1), true);
        if ("POST".equals(metodo) && caminho.matches("/api/v1/anuncios/[^/]+/fotos"))
            return regra("upload-foto-anuncio", 30, Duration.ofHours(1), true);
        if ("PUT".equals(metodo) && caminho.matches("/api/v1/usuarios/[^/]+/foto"))
            return regra("upload-foto-perfil", 10, Duration.ofHours(1), true);
        if ("POST".equals(metodo) && caminho.matches("/api/v1/anuncios/[^/]+/contato/whatsapp"))
            return regra("contato-whatsapp", 10, Duration.ofHours(1), true);
        return null;
    }

    private RegraHttp regra(String nome, int limite, Duration janela, boolean porUsuario) {
        var configurada = settings.regra(nome, limite, janela);
        return new RegraHttp(configurada.nome(), configurada.limite(), configurada.janela(), porUsuario);
    }

    private String identificador(HttpServletRequest request, boolean porUsuario) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (porUsuario && authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) return "usuario:" + authentication.getName();
        return "ip:" + request.getRemoteAddr();
    }

    private record RegraHttp(String nome, int limite, Duration janela, boolean porUsuario) { }
}
