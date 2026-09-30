package repasse.phcauto.backend.compliance.internal.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import repasse.phcauto.backend.compliance.AceiteInvalidoException;
import repasse.phcauto.backend.compliance.RegistrarAceiteCadastroCommand;

import repasse.phcauto.backend.compliance.internal.core.domain.AceiteTermos;
import repasse.phcauto.backend.compliance.internal.core.domain.VersoesDocumentosVigentes;
import repasse.phcauto.backend.compliance.internal.core.gateway.AceiteTermosGateway;
import repasse.phcauto.backend.compliance.internal.core.usecase.RegistrarAceite;
class RegistrarAceiteTests {
    private final AceiteTermosGateway gateway = mock(AceiteTermosGateway.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC);
    private final RegistrarAceite useCase = new RegistrarAceite(gateway,
            new VersoesDocumentosVigentes("2.1", "3.0"), clock);

    @Test
    void registraVersoesVigentesCliqueRedeEHorarioDoServidor() {
        UUID usuarioId = UUID.randomUUID();
        Instant clique = clock.instant().minusSeconds(30);

        useCase.execute(new RegistrarAceiteCadastroCommand(usuarioId, true, clique,
                "2.1", "3.0", "203.0.113.10"));

        var captor = ArgumentCaptor.forClass(AceiteTermos.class);
        verify(gateway).salvar(captor.capture());
        var aceite = captor.getValue();
        assertEquals(usuarioId, aceite.usuarioId());
        assertEquals(clique, aceite.aceiteEm());
        assertEquals(clock.instant(), aceite.registradoEm());
        assertEquals("2.1", aceite.versaoTermosUso());
        assertEquals("3.0", aceite.versaoPoliticaPrivacidade());
        assertEquals("203.0.113.10", aceite.enderecoRede());
    }

    @Test
    void rejeitaAusenciaDeAceiteVersaoDivergenteHorarioInvalidoERedeVazia() {
        UUID id = UUID.randomUUID();
        assertThrows(AceiteInvalidoException.class, () -> useCase.execute(
                new RegistrarAceiteCadastroCommand(id, false, clock.instant(), "2.1", "3.0", "127.0.0.1")));
        assertThrows(AceiteInvalidoException.class, () -> useCase.execute(
                new RegistrarAceiteCadastroCommand(id, true, clock.instant(), "antiga", "3.0", "127.0.0.1")));
        assertThrows(AceiteInvalidoException.class, () -> useCase.execute(
                new RegistrarAceiteCadastroCommand(id, true, clock.instant().minusSeconds(86_401), "2.1", "3.0", "127.0.0.1")));
        assertThrows(AceiteInvalidoException.class, () -> useCase.execute(
                new RegistrarAceiteCadastroCommand(id, true, clock.instant(), "2.1", "3.0", " ")));
        verifyNoInteractions(gateway);
    }
}
