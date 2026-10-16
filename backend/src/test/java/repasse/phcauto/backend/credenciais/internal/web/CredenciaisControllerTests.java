package repasse.phcauto.backend.credenciais.internal.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import repasse.phcauto.backend.autenticacao.SegundoFator;
import repasse.phcauto.backend.credenciais.internal.security.CredentialCookieService;
import repasse.phcauto.backend.credenciais.internal.service.CredenciaisService;
import repasse.phcauto.backend.usuarios.UsuarioSeguranca;

class CredenciaisControllerTests {
    @Test void trocaExigeConfirmacaoDaNovaSenha() {
        assertThrows(IllegalArgumentException.class,
                () -> new CredenciaisController.ConfirmarRequest("token", "Nova@123", "Outra@123"));
    }

    @Test void mfaHabilitadoEmiteDesafioSemAlterarSenha() {
        var credenciais = mock(CredenciaisService.class);
        var segundoFator = mock(SegundoFator.class);
        var controller = new CredenciaisController(credenciais, mock(CredentialCookieService.class), segundoFator);
        var auth = mock(Authentication.class);
        var http = mock(HttpServletRequest.class);
        var usuarioId = UUID.randomUUID();
        var usuario = new UsuarioSeguranca(usuarioId, "pessoa@example.com", "CLIENTE", 3, true);
        when(auth.getName()).thenReturn(usuarioId.toString());
        when(credenciais.validarContaParaTroca(usuarioId)).thenReturn(usuario);
        when(segundoFator.habilitado()).thenReturn(true);
        when(segundoFator.criar(usuario, null)).thenReturn(new SegundoFator.Desafio("desafio", 300, "p***@example.com"));

        var resposta = controller.trocar(auth, http);

        assertEquals(202, resposta.getStatusCode().value());
        assertEquals("SEGUNDO_FATOR_NECESSARIO", resposta.getBody().status());
        verify(credenciais, never()).confirmar(anyString(), anyString());
    }

    @Test void codigoValidoApenasEmiteAutorizacaoDeUsoUnico() {
        var credenciais = mock(CredenciaisService.class);
        var segundoFator = mock(SegundoFator.class);
        var controller = new CredenciaisController(credenciais, mock(CredentialCookieService.class), segundoFator);
        var auth = mock(Authentication.class); var http = mock(HttpServletRequest.class);
        var usuarioId = UUID.randomUUID();
        var usuario = new UsuarioSeguranca(usuarioId, "pessoa@example.com", "CLIENTE", 3, true);
        when(auth.getName()).thenReturn(usuarioId.toString());
        when(credenciais.validarContaParaTroca(usuarioId)).thenReturn(usuario);
        when(credenciais.autorizar(eq(usuarioId), any())).thenReturn("autorizacao");

        var resposta = controller.confirmarTroca(new CredenciaisController.AutorizarRequest("desafio", "123456"), auth, http);

        assertEquals("AUTORIZADA", resposta.getBody().status());
        assertEquals("autorizacao", resposta.getBody().token());
        verify(segundoFator).validar("desafio", "123456", usuarioId, 3, null);
        verify(credenciais, never()).confirmar(anyString(), anyString());
    }
}
