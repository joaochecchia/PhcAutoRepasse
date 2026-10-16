package repasse.phcauto.backend.infra.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import repasse.phcauto.backend.infra.config.*;

@SpringJUnitWebConfig(SecurityHttpTests.Config.class)
@TestPropertySource(properties = "JWT_SECRET=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class SecurityHttpTests {
    @Configuration @EnableWebMvc @Import({SecurityConfig.class, JwtConfig.class, Endpoints.class, repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller.UsuariosController.class})
    static class Config {
        @Bean repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade usuarios() {
            return (repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade)
                    java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                            new Class<?>[]{repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade.class},
                            (proxy, method, args) -> switch (method.getName()) {
                                case "hashCode" -> System.identityHashCode(proxy);
                                case "equals" -> proxy == args[0];
                                case "toString" -> "UsuariosFacadeTestDouble";
                                default -> null;
                            });
        }
        @Bean repasse.phcauto.backend.infra.security.ratelimit.RateLimitFilter rateLimitFilter() {
            var limiter = new repasse.phcauto.backend.infra.security.ratelimit.RateLimiter() {
                public void consumir(String regra, String identificador, int limite, java.time.Duration janela) { }
                public void limpar(String regra, String identificador) { }
            };
            return new repasse.phcauto.backend.infra.security.ratelimit.RateLimitFilter(limiter,
                    new repasse.phcauto.backend.infra.security.ratelimit.RateLimitSettings(
                            new org.springframework.mock.env.MockEnvironment()));
        }
        @Bean repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtSessionVersionValidator sessionVersionValidator() {
            return new repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtSessionVersionValidator(null) {
                @Override public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(
                        org.springframework.security.oauth2.jwt.Jwt token) {
                    return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                }
            };
        }
        @Bean repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService authCookieService() {
            return new repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService(false);
        }
    }
    @RestController static class Endpoints {
        @GetMapping({"/swagger-ui/index.html", "/v3/api-docs", "/api/v1/anuncios", "/api/v1/assinaturas/planos"})
        String get() { return "ok"; }
        @PostMapping({"/api/v1/autenticacao/login", "/api/v1/usuarios"})
        String post() { return "ok"; }
    }
    @Autowired WebApplicationContext context;
    @Autowired JwtEncoder encoder;
    MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    String token(String role, String issuer, Instant expires, JwtEncoder signer) {
        var claims = JwtClaimsSet.builder().subject("00000000-0000-0000-0000-000000000001")
                .issuer(issuer).issuedAt(Instant.now().minusSeconds(600)).expiresAt(expires)
                .claim("roles", java.util.List.of(role)).build();
        return signer.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    @Test void swaggerBuscaPublicosSemSessaoEEmitemCookieCsrf() throws Exception {
        for (String path : new String[]{"/swagger-ui/index.html", "/v3/api-docs", "/api/v1/anuncios"})
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(cookie().exists("XSRF-TOKEN"))
                    .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
                    .andExpect(cookie().doesNotExist("JSESSIONID"));
    }
    @Test void exigeCsrfNoLoginDoNavegadorEAceitaDoubleSubmitCookie() throws Exception {
        mvc.perform(post("/api/v1/autenticacao/login"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Token CSRF inválido"));
        var cookie = mvc.perform(get("/api/v1/anuncios")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        mvc.perform(post("/api/v1/autenticacao/login").cookie(cookie)
                        .header("X-XSRF-TOKEN", cookie.getValue()))
                .andExpect(status().isOk());
    }
    @Test void protegeRotasESoAceitaBearerValido() throws Exception {
        mvc.perform(get("/api/v1/usuarios/00000000-0000-0000-0000-000000000001")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.title").value("Não autenticado"));
        mvc.perform(get("/api/v1/usuarios/00000000-0000-0000-0000-000000000001").header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
        var valid = token("CLIENTE", "phcauto", Instant.now().plusSeconds(300), encoder);
        mvc.perform(get("/api/v1/usuarios/00000000-0000-0000-0000-000000000001").header("Authorization", "Bearer " + valid))
                .andExpect(status().isOk()).andExpect(cookie().doesNotExist("JSESSIONID"));
        mvc.perform(get("/api/v1/usuarios/00000000-0000-0000-0000-000000000001")
                        .cookie(new jakarta.servlet.http.Cookie("PHC_AUTH", valid)))
                .andExpect(status().isOk()).andExpect(cookie().doesNotExist("JSESSIONID"));
        mvc.perform(get("/api/v1/assinaturas/planos").header("Authorization", "Bearer " + valid)).andExpect(status().isForbidden());
        var admin = token("ADMIN", "phcauto", Instant.now().plusSeconds(300), encoder);
        mvc.perform(get("/api/v1/assinaturas/planos").header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/usuarios").header("Authorization", "Bearer " + valid)).andExpect(status().isForbidden());
    }
    @Test void usuarioNaoAcessaOutroCadastro() throws Exception {
        var valid = token("CLIENTE", "phcauto", Instant.now().plusSeconds(300), encoder);
        var outro = "/api/v1/usuarios/00000000-0000-0000-0000-000000000002";
        mvc.perform(get(outro).header("Authorization", "Bearer " + valid)).andExpect(status().isForbidden());
        mvc.perform(patch(outro).header("Authorization", "Bearer " + valid)
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(delete(outro).header("Authorization", "Bearer " + valid)).andExpect(status().isForbidden());
        var admin = token("ADMIN", "phcauto", Instant.now().plusSeconds(300), encoder);
        mvc.perform(delete(outro).header("Authorization", "Bearer " + admin)).andExpect(status().isForbidden());
        var dono = token("DONO", "phcauto", Instant.now().plusSeconds(300), encoder);
        mvc.perform(delete(outro).header("Authorization", "Bearer " + dono)).andExpect(status().isNoContent());
    }
    @Test void corsPermiteFrontendEBloqueiaOrigemDesconhecida() throws Exception {
        mvc.perform(options("/api/v1/usuarios/registrar")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsStringIgnoringCase("x-xsrf-token")));
        mvc.perform(options("/api/v1/usuarios/registrar")
                        .header("Origin", "https://origem-desconhecida.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
    @Test void rejeitaExpiracaoEmissorEAssinaturaInvalidos() throws Exception {
        byte[] otherKey = new byte[32];
        java.util.Arrays.fill(otherKey, (byte) 42);
        var other = NimbusJwtEncoder.withSecretKey(new SecretKeySpec(otherKey, "HmacSHA256")).build();
        for (String token : new String[]{
                token("CLIENTE", "phcauto", Instant.now().minusSeconds(120), encoder),
                token("CLIENTE", "outro", Instant.now().plusSeconds(300), encoder),
                token("CLIENTE", "phcauto", Instant.now().plusSeconds(300), other)}) {
            mvc.perform(get("/api/v1/usuarios/00000000-0000-0000-0000-000000000001").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        }
    }
}
