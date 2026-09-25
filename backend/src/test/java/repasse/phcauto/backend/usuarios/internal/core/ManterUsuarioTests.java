package repasse.phcauto.backend.usuarios.internal.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.request.EnderecoPatchRequest;

class ManterUsuarioTests {
    private static final UUID ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-25T12:00:00Z"), ZoneOffset.UTC);

    private UsuarioCompleto pf() {
        return new UsuarioCompleto(ID, TipoPessoa.PF, "Nome", "email@example.com", "11999999999",
                true, "hash", "52998224725", LocalDate.of(1990, 1, 1), null, null,
                new EnderecoUsuarioDados("01001000", "São Paulo", "Sé", "Rua", "1", "apto", "SP"),
                CLOCK.instant().minusSeconds(60), CLOCK.instant().minusSeconds(60));
    }

    @Test void patchDeEnderecoMantemDemaisTabelasEPublicaDepoisDaAtualizacao() {
        var consultas = mock(ConsultarUsuarioGateway.class);
        var atualizacoes = mock(AtualizarUsuarioGateway.class);
        var senhas = mock(HashSenhaGateway.class);
        var eventos = mock(PublicarUsuarioAlteradoGateway.class);
        when(consultas.buscar(ID)).thenReturn(pf());
        when(atualizacoes.atualizar(eq(ID), any(), eq(CLOCK.instant()))).thenReturn(pf());
        var useCase = new AtualizarUsuario(consultas, atualizacoes, senhas, eventos, CLOCK);

        useCase.execute(ID, new AtualizarUsuarioRequest(null, null, null, null, null, null,
                null, null, new EnderecoPatchRequest(null, "Campinas", null, null, null, "", null)));

        var captor = ArgumentCaptor.forClass(AlteracoesUsuario.class);
        var ordem = inOrder(atualizacoes, eventos);
        ordem.verify(atualizacoes).atualizar(eq(ID), captor.capture(), eq(CLOCK.instant()));
        ordem.verify(eventos).publicar(any(UsuarioAlterado.class));
        assertNull(captor.getValue().nome());
        assertEquals("Campinas", captor.getValue().endereco().cidade());
        assertNull(captor.getValue().endereco().complemento());
        verifyNoInteractions(senhas);
    }

    @Test void patchDeSenhaGeraHashENuncaEnviaTextoPlano() {
        var consultas = mock(ConsultarUsuarioGateway.class);
        var atualizacoes = mock(AtualizarUsuarioGateway.class);
        var senhas = mock(HashSenhaGateway.class);
        var eventos = mock(PublicarUsuarioAlteradoGateway.class);
        when(consultas.buscar(ID)).thenReturn(pf());
        when(senhas.gerar("nova-senha")).thenReturn("novo-hash");
        when(atualizacoes.atualizar(eq(ID), any(), any())).thenReturn(pf());
        new AtualizarUsuario(consultas, atualizacoes, senhas, eventos, CLOCK).execute(ID,
                new AtualizarUsuarioRequest(null, null, null, "nova-senha", null, null,
                        null, null, null));
        var captor = ArgumentCaptor.forClass(AlteracoesUsuario.class);
        verify(atualizacoes).atualizar(eq(ID), captor.capture(), any());
        assertEquals("novo-hash", captor.getValue().senhaHash());
        assertFalse(captor.getValue().toString().contains("nova-senha"));
    }

    @Test void rejeitaPatchVazioEOsCamposDoOutroTipo() {
        var consultas = mock(ConsultarUsuarioGateway.class);
        when(consultas.buscar(ID)).thenReturn(pf());
        var useCase = new AtualizarUsuario(consultas, mock(AtualizarUsuarioGateway.class),
                mock(HashSenhaGateway.class), mock(PublicarUsuarioAlteradoGateway.class), CLOCK);
        assertThrows(CadastroInvalidoException.class, () -> useCase.execute(ID,
                new AtualizarUsuarioRequest(null, null, null, null, null, null, null, null, null)));
        assertThrows(CadastroInvalidoException.class, () -> useCase.execute(ID,
                new AtualizarUsuarioRequest(null, null, null, null, null, null,
                        "11222333000181", null, null)));
    }

    @Test void exclusaoPublicaSomenteDepoisDeRemoverAgregado() {
        var gateway = mock(ExcluirUsuarioGateway.class);
        var eventos = mock(PublicarUsuarioAlteradoGateway.class);
        when(gateway.excluir(ID)).thenReturn(pf());
        new ExcluirUsuario(gateway, eventos, CLOCK).execute(ID);
        var ordem = inOrder(gateway, eventos);
        ordem.verify(gateway).excluir(ID);
        ordem.verify(eventos).publicar(argThat(evento -> evento.usuarioId().equals(ID)
                && evento.operacao() == UsuarioAlterado.Operacao.EXCLUIDO));
    }
}
