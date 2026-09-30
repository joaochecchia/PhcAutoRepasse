package repasse.phcauto.backend.usuarios.internal.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;
import repasse.phcauto.backend.usuarios.UsuarioCriado;

import repasse.phcauto.backend.usuarios.internal.core.domain.CriarUsuarioCommand;
import repasse.phcauto.backend.usuarios.internal.core.domain.DadosNovoUsuario;
import repasse.phcauto.backend.usuarios.internal.core.domain.EnderecoCadastro;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroDuplicadoException;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.core.gateway.HashSenhaGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.PublicarUsuarioCriadoGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.UsuarioGateway;
import repasse.phcauto.backend.usuarios.internal.core.usecase.CriarUsuario;
import repasse.phcauto.backend.usuarios.internal.core.usecase.CriarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.validation.ValidacaoCadastro;
class CriarUsuarioTests {
    private final UsuarioGateway usuarios = mock(UsuarioGateway.class);
    private final HashSenhaGateway senhas = mock(HashSenhaGateway.class);
    private final PublicarUsuarioCriadoGateway eventos = mock(PublicarUsuarioCriadoGateway.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-25T12:00:00Z"), ZoneOffset.UTC);
    private final CriarUsuarioUseCase useCase = new CriarUsuario(usuarios, senhas, eventos, clock);

    private CriarUsuarioCommand command(LocalDate nascimento) {
        return new CriarUsuarioCommand(TipoPessoa.PF, " Cliente ", "CLIENTE@EXAMPLE.COM", "11999999999",
                "senha-de-teste", "52998224725", nascimento, null, null,
                new EnderecoCadastro("01001000", "São Paulo", "Sé", "Praça da Sé", null, null, "sp"));
    }

    @Test void salvaAntesDePublicarESemTransportarSenhaEmTextoAoGateway() {
        when(senhas.gerar("senha-de-teste")).thenReturn("hash-seguro");
        var resultado = useCase.execute(command(LocalDate.of(1990, 1, 1)));
        var dados = ArgumentCaptor.forClass(DadosNovoUsuario.class);
        var evento = ArgumentCaptor.forClass(UsuarioCriado.class);
        var ordem = inOrder(usuarios, eventos);
        ordem.verify(usuarios).existeEmail("cliente@example.com");
        ordem.verify(usuarios).existeDocumento(TipoPessoa.PF, "52998224725");
        ordem.verify(usuarios).salvarCadastro(eq(resultado.id()), dados.capture(), eq(clock.instant()));
        ordem.verify(eventos).publicar(evento.capture());
        assertEquals("hash-seguro", dados.getValue().senhaHash());
        assertEquals(resultado.id(), evento.getValue().usuarioId());
        assertEquals("Cliente", resultado.nome());
        assertFalse(command(LocalDate.of(1990, 1, 1)).toString().contains("senha-de-teste"));
    }

    @Test void duplicidadeNaoGeraHashNemPersisteNemPublica() {
        when(usuarios.existeEmail(anyString())).thenReturn(true);
        assertThrows(CadastroDuplicadoException.class, () -> useCase.execute(command(LocalDate.of(1990, 1, 1))));
        verifyNoInteractions(senhas, eventos);
        verify(usuarios, never()).salvarCadastro(any(), any(), any());
    }

    @Test void falhaDePersistenciaNaoPublicaEvento() {
        when(senhas.gerar(anyString())).thenReturn("hash");
        doThrow(new IllegalStateException("falha")).when(usuarios).salvarCadastro(any(), any(), any());
        assertThrows(IllegalStateException.class, () -> useCase.execute(command(LocalDate.of(1990, 1, 1))));
        verifyNoInteractions(eventos);
    }

    @Test void rejeitaMenorDeDezoitoAnosEAceitaAniversarioDeDezoitoHoje() {
        assertThrows(CadastroInvalidoException.class,
                () -> useCase.execute(command(LocalDate.now(clock).minusYears(18).plusDays(1))));
        verifyNoInteractions(usuarios, senhas, eventos);

        when(senhas.gerar(anyString())).thenReturn("hash");
        assertDoesNotThrow(() -> useCase.execute(command(LocalDate.now(clock).minusYears(18))));
    }

    @Test void adminExigePerfilCompletoEDonoPersisteSomenteUsuario() {
        when(senhas.gerar(anyString())).thenReturn("hash");
        var admin = new CriarUsuarioCommand(TipoPessoa.PF,"admin","admin@email.com","+5561999999999",
                "senha123","52998224725",LocalDate.of(2004,1,1),null,null,null,PapelUsuario.ADMIN);
        var resultadoAdmin=useCase.execute(admin);
        var dados=ArgumentCaptor.forClass(DadosNovoUsuario.class);
        verify(usuarios).salvarCadastro(eq(resultadoAdmin.id()),dados.capture(),eq(clock.instant()));
        assertEquals(PapelUsuario.ADMIN,dados.getValue().papel());
        assertEquals("52998224725",dados.getValue().cpf());

        reset(usuarios,senhas,eventos); when(senhas.gerar(anyString())).thenReturn("hash");
        var dono=new CriarUsuarioCommand(null,"Dono","dono@email.com",null,"senha123",
                null,null,null,null,null,PapelUsuario.DONO);
        var resultadoDono=useCase.execute(dono);
        verify(usuarios).existeEmail("dono@email.com");
        verify(usuarios,never()).existeDocumento(any(),any());
        verify(usuarios).salvarCadastro(eq(resultadoDono.id()),argThat(d -> d.papel()==PapelUsuario.DONO
                && d.cpf()==null && d.cnpj()==null && d.endereco()==null),eq(clock.instant()));
    }

    @Test void rejeitaDocumentosInvalidosEPerfisMisturados() {
        assertFalse(ValidacaoCadastro.documentoValido("11111111111", true));
        assertFalse(ValidacaoCadastro.documentoValido("52998224724", true));
        assertTrue(ValidacaoCadastro.documentoValido("11222333000181", false));
        var c = command(LocalDate.of(1990, 1, 1));
        assertThrows(CadastroInvalidoException.class, () -> new CriarUsuarioCommand(c.tipoPessoa(), c.nome(),
                c.email(), c.telefone(), c.senha(), c.cpf(), c.dataNascimento(), "11222333000181", null, c.endereco()));
    }
}
