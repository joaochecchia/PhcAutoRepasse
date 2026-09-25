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
import repasse.phcauto.backend.usuarios.internal.infrastructure.controller.LoginController;
import repasse.phcauto.backend.usuarios.internal.core.Login;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write.UsuarioWriteRepository;

class LoginHttpTests {
    private final UsuarioWriteRepository repository = mock(UsuarioWriteRepository.class);
    private final org.springframework.security.crypto.password.PasswordEncoder encoder = new UsuariosConfiguration().usuarioPasswordEncoder();
    private MockMvc mvc;

    @BeforeEach void configurar() {
        var gateway = new DatabaseAutenticacaoGateway(repository, encoder);
        mvc = MockMvcBuilders.standaloneSetup(new LoginController(new Login(gateway)))
                .setControllerAdvice(new LoginExceptionHandler()).build();
    }
    private UsuarioEntity usuario(String hash, boolean ativo) {
        var usuario = mock(UsuarioEntity.class);
        when(usuario.getSenhaHash()).thenReturn(hash);
        when(usuario.getAtivo()).thenReturn(ativo);
        return usuario;
    }
    private org.springframework.test.web.servlet.ResultActions login(String senha) throws Exception {
        return mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                .content("{\"email\":\"CLIENTE@example.com\",\"senha\":\""+senha+"\"}"));
    }
    @Test void autenticaHashDoCadastroERetornaSomenteMensagem() throws Exception {
        String hash = new UsuariosConfiguration().hashSenhaGateway(encoder).gerar("Senha-123");
        doReturn(List.of(usuario(hash, true))).when(repository).findTop2ByEmailIgnoreCase("cliente@example.com");
        login("Senha-123").andExpect(status().isOk())
                .andExpect(content().json("{\"mensagem\":\"Login realizado com sucesso\"}"))
                .andExpect(jsonPath("$.length()").value(1));
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
    @Test void aceitaBcryptIdentificadoSemMudarCore() throws Exception {
        String hash = "{bcrypt}" + new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Senha-123");
        doReturn(List.of(usuario(hash, true))).when(repository).findTop2ByEmailIgnoreCase(anyString());
        login("Senha-123").andExpect(status().isOk());
    }
}
