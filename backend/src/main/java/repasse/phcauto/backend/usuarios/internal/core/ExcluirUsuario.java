package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;

public final class ExcluirUsuario implements ExcluirUsuarioUseCase {
    private final ExcluirUsuarioGateway usuarios;
    private final PublicarUsuarioAlteradoGateway eventos;
    private final Clock clock;

    public ExcluirUsuario(ExcluirUsuarioGateway usuarios,
            PublicarUsuarioAlteradoGateway eventos, Clock clock) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.eventos = Objects.requireNonNull(eventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override public void execute(UUID usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        var removido = usuarios.excluir(usuarioId);
        eventos.publicar(new UsuarioAlterado(UUID.randomUUID(), usuarioId, removido.tipoPessoa(),
                UsuarioAlterado.Operacao.EXCLUIDO, clock.instant()));
    }
}
