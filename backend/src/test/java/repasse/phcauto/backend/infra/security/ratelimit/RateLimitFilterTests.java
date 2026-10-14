package repasse.phcauto.backend.infra.security.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTests {

    @Test
    void aplicaRegraConfiguradaAoLogin() throws Exception {
        var chamada = new AtomicReference<Chamada>();
        RateLimiter limiter = new RateLimiter() {
            public void consumir(String regra, String identificador, int limite, Duration janela) {
                chamada.set(new Chamada(regra, identificador, limite, janela));
            }
            public void limpar(String regra, String identificador) { }
        };
        var environment = new MockEnvironment()
                .withProperty("app.rate-limit.login.limit", "3")
                .withProperty("app.rate-limit.login.window", "2m");
        var filter = new RateLimitFilter(limiter, new RateLimitSettings(environment));
        var request = new MockHttpServletRequest("POST", "/api/v1/usuarios/login");
        request.setRemoteAddr("192.0.2.10");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(new Chamada("login", "ip:192.0.2.10", 3, Duration.ofMinutes(2)), chamada.get());
        assertEquals(200, response.getStatus());
    }

    @Test
    void devolve429ComRetryAfterQuandoLimiteEsgota() throws Exception {
        RateLimiter limiter = new RateLimiter() {
            public void consumir(String regra, String identificador, int limite, Duration janela) {
                throw new RateLimitExceededException(60);
            }
            public void limpar(String regra, String identificador) { }
        };
        var filter = new RateLimitFilter(limiter, new RateLimitSettings(new MockEnvironment()));
        var request = new MockHttpServletRequest("GET", "/api/v1/anuncios");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(429, response.getStatus());
        assertEquals("60", response.getHeader("Retry-After"));
        assertEquals("application/problem+json;charset=UTF-8", response.getContentType());
    }

    private record Chamada(String regra, String identificador, int limite, Duration janela) { }
}
