package repasse.phcauto.backend.infra.database.sync;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

class PublicationRecoveryTests {
    @Test void recuperaEventosDeUsuarioEProjecaoSemIncluirEventosDesconhecidos() {
        for (var operacao : UsuarioAlterado.Operacao.values()) {
            assertTrue(PublicationRecovery.supports(new UsuarioAlterado(UUID.randomUUID(),
                    UUID.randomUUID(), TipoPessoa.PF, operacao, Instant.now())));
        }
        assertTrue(PublicationRecovery.supports(RowChanged.of(ProjectionTable.USUARIOS,
                List.of(UUID.randomUUID().toString()))));
        assertFalse(PublicationRecovery.supports("evento desconhecido"));
    }
}
