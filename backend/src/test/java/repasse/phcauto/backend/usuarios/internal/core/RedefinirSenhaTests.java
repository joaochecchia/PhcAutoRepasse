package repasse.phcauto.backend.usuarios.internal.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.internal.core.domain.RedefinirSenhaCommand;
import repasse.phcauto.backend.usuarios.internal.core.usecase.RedefinirSenha;

class RedefinirSenhaTests {
    @Test void geraHashPersisteEPublicaEventoSemExporSenha() {
        var id = UUID.randomUUID();
        var instante = Instant.parse("2026-10-13T12:00:00Z");
        var gravado = new ArrayList<String>();
        var eventos = new ArrayList<UsuarioAlterado>();
        var sessoesRevogadas = new ArrayList<UUID>();
        var useCase = new RedefinirSenha((usuarioId, hash, atualizadoEm) -> {
            assertEquals(id, usuarioId);
            assertEquals(instante, atualizadoEm);
            gravado.add(hash);
            return TipoPessoa.PF;
        }, senha -> "hash-seguro", eventos::add, (usuarioId, agora) -> {
            assertEquals(instante, agora);
            sessoesRevogadas.add(usuarioId);
        }, Clock.fixed(instante, ZoneOffset.UTC));

        var command = new RedefinirSenhaCommand("Senha@123");
        useCase.execute(id, command);

        assertEquals(java.util.List.of("hash-seguro"), gravado);
        assertFalse(command.toString().contains("Senha@123"));
        assertEquals(java.util.List.of(id), sessoesRevogadas);
        assertEquals(UsuarioAlterado.Operacao.ATUALIZADO, eventos.get(0).operacao());
    }
}
