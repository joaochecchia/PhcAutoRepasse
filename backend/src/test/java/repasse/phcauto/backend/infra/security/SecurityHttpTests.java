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
            return org.mockito.Mockito.mock(repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade.class);
        }
    }
    @RestController static class Endpoints {
        @GetMapping({"/swagger-ui/index.html", "/v3/api-docs", "/api/v1/anuncios", "/api/v1/assinaturas/planos"})
        String get() { return "ok"; }
        @PostMapping({"/api/v1/usuarios/login", "/api/v1/usuarios"})
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
    @Test void swaggerBuscaCadastroELoginPublicosSemSessao() throws Exception {
        for (String path : new String[]{"/swagger-ui/index.html", "/v3/api-docs", "/api/v1/anuncios"})
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(cookie().doesNotExist("JSESSIONID"));
        for (String path : new String[]{"/api/v1/usuarios/login"})
            mvc.perform(post(path)).andExpect(status().isOk());
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
        mvc.perform(get("/api/v1/usuarios/00000000-0000-0000-0000-000000000002")
                .header("Authorization", "Bearer " + valid)).andExpect(status().isForbidden());
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
