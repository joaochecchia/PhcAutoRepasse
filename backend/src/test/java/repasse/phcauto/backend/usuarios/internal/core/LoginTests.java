package repasse.phcauto.backend.usuarios.internal.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.usuarios.request.LoginRequest;

class LoginTests {
    @Test void delegaCredenciaisNormalizadasSemAlterarSenha() {
        var gateway = mock(AutenticacaoGateway.class);
        new Login(gateway).execute(new LoginRequest(" CLIENTE@EXAMPLE.COM ", " senha "));
        verify(gateway).autenticar("cliente@example.com", " senha ");
        verifyNoMoreInteractions(gateway);
    }
    @Test void propagaRejeicaoDoGateway() {
        var gateway = mock(AutenticacaoGateway.class);
        doThrow(new CredenciaisInvalidasException()).when(gateway).autenticar(anyString(), anyString());
        assertThrows(CredenciaisInvalidasException.class,
                () -> new Login(gateway).execute(new LoginRequest("cliente@example.com", "senha")));
    }
    @Test void rejeitaEntradaInvalidaSemExporSenha() {
        assertThrows(IllegalArgumentException.class, () -> new LoginRequest(null, "segredo"));
        assertThrows(IllegalArgumentException.class, () -> new LoginRequest("a@example.com", " "));
        assertFalse(new LoginRequest("a@example.com", "segredo").toString().contains("segredo"));
    }
}
