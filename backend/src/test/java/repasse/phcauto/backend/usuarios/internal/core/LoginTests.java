package repasse.phcauto.backend.usuarios.internal.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.usuarios.internal.core.domain.LoginCommand;

import repasse.phcauto.backend.usuarios.internal.core.exception.CredenciaisInvalidasException;
import repasse.phcauto.backend.usuarios.internal.core.gateway.AutenticacaoGateway;
import repasse.phcauto.backend.usuarios.internal.core.usecase.Login;
class LoginTests {
    @Test void delegaCredenciaisNormalizadasSemAlterarSenha() {
        var gateway = mock(AutenticacaoGateway.class);
        new Login(gateway).execute(new LoginCommand(" CLIENTE@EXAMPLE.COM ", " senha "));
        verify(gateway).autenticar("cliente@example.com", " senha ");
        verifyNoMoreInteractions(gateway);
    }
    @Test void propagaRejeicaoDoGateway() {
        var gateway = mock(AutenticacaoGateway.class);
        doThrow(new CredenciaisInvalidasException()).when(gateway).autenticar(anyString(), anyString());
        assertThrows(CredenciaisInvalidasException.class,
                () -> new Login(gateway).execute(new LoginCommand("cliente@example.com", "senha")));
    }
    @Test void rejeitaEntradaInvalidaSemExporSenha() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand(null, "segredo"));
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand("a@example.com", " "));
        assertFalse(new LoginCommand("a@example.com", "segredo").toString().contains("segredo"));
    }
}
