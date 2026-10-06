package repasse.phcauto.backend.usuarios.internal.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller.LoginController;
import repasse.phcauto.backend.usuarios.internal.core.usecase.Login;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;

import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.exception.LoginExceptionHandler;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway.DatabaseAutenticacaoGateway;
import repasse.phcauto.backend.usuarios.internal.infrastructure.configuration.AutenticacaoConfiguration;
import repasse.phcauto.backend.usuarios.internal.infrastructure.configuration.UsuariosConfiguration;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtLoginService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.UsuarioDetailsService;
class LoginHttpTests {
    private final UsuarioWriteRepository repository = mock(UsuarioWriteRepository.class);
    private final org.springframework.security.crypto.password.PasswordEncoder encoder = new UsuariosConfiguration().usuarioPasswordEncoder();
    private MockMvc mvc;

    @BeforeEach void configurar() {
        var manager = new AutenticacaoConfiguration().authenticationManager(new UsuarioDetailsService(repository), encoder);
        var gateway = new DatabaseAutenticacaoGateway(manager);
        var jwtEncoder = org.springframework.security.oauth2.jwt.NimbusJwtEncoder.withSecretKey(
                new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256")).build();
        var service = new JwtLoginService(new Login(gateway), jwtEncoder, java.time.Clock.systemUTC(), "phcauto", 900);
        mvc = MockMvcBuilders.standaloneSetup(new LoginController(service,
                        new repasse.phcauto.backend.usuarios.internal.infrastructure.security.AuthCookieService(false)))
                .setControllerAdvice(new LoginExceptionHandler()).build();
    }
    private UsuarioEntity usuario(String hash, boolean ativo) {
        var usuario = mock(UsuarioEntity.class);
        when(usuario.getId()).thenReturn(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(usuario.getPapel()).thenReturn(repasse.phcauto.backend.domain.model.identidade.PapelUsuario.CLIENTE);
        when(usuario.getSenhaHash()).thenReturn(hash);
        when(usuario.getAtivo()).thenReturn(ativo);
        return usuario;
    }
    private org.springframework.test.web.servlet.ResultActions login(String senha) throws Exception {
        return mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                .content("{\"email\":\"CLIENTE@example.com\",\"senha\":\""+senha+"\"}"));
    }
    @Test void autenticaHashDoCadastroERetornaCookieHttpOnly() throws Exception {
        String hash = new UsuariosConfiguration().hashSenhaGateway(encoder).gerar("Senha-123");
        doReturn(List.of(usuario(hash, true))).when(repository).findTop2ByEmailIgnoreCase("cliente@example.com");
        login("Senha-123").andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.usuarioId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.papel").value("CLIENTE"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(cookie().httpOnly("PHC_AUTH", true))
                .andExpect(cookie().maxAge("PHC_AUTH", 900))
                .andExpect(header().string("Cache-Control", "no-store"));
    }
    @Test void senhaIncorretaRecebe401() throws Exception {
        doReturn(List.of(usuario(encoder.encode("correta"), true))).when(repository).findTop2ByEmailIgnoreCase(anyString());
        login("incorreta").andExpect(status().isUnauthorized()).andExpect(jsonPath("detail").value("Email ou senha inválidos"));
    }
    @Test void contaInexistenteInativaSocialOuHashInvalidoRecebeMesmaResposta() throws Exception {
        for (var usuarios : List.of(List.<UsuarioEntity>of(), List.of(usuario(encoder.encode("Senha-123"), false)),
                List.of(usuario(null, true)), List.of(usuario("texto-plano", true)))) {
            when(repository.findTop2ByEmailIgnoreCase(anyString())).thenReturn(usuarios);
            login("Senha-123").andExpect(status().isUnauthorized()).andExpect(jsonPath("detail").value("Email ou senha inválidos"));
        }
    }
    @Test void rejeitaEmailLegadoAmbiguo() throws Exception {
        var usuario = usuario(encoder.encode("Senha-123"), true);
        when(repository.findTop2ByEmailIgnoreCase(anyString())).thenReturn(List.of(usuario, usuario));
        login("Senha-123").andExpect(status().isUnauthorized());
    }
    @Test void entradaInvalidaRetorna400() throws Exception {
        mvc.perform(post("/api/v1/usuarios/login").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
    @Test void aceitaPbkdf2Legado() throws Exception {
        String hash = "{pbkdf2@SpringSecurity_v5_8}" +
                org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("Senha-123");
        doReturn(List.of(usuario(hash, true))).when(repository).findTop2ByEmailIgnoreCase(anyString());
        login("Senha-123").andExpect(status().isOk());
    }
    @Test void novosHashesUsamBcrypt() {
        assertTrue(encoder.encode("Senha-123").startsWith("{bcrypt}$2"));
    }
    @Test void aceitaBcryptIdentificadoSemMudarCore() throws Exception {
        String hash = "{bcrypt}" + new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Senha-123");
        doReturn(List.of(usuario(hash, true))).when(repository).findTop2ByEmailIgnoreCase(anyString());
        login("Senha-123").andExpect(status().isOk());
    }
}
