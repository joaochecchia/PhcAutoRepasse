package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.usuarios.internal.core.domain.RedefinirSenhaCommand;
import repasse.phcauto.backend.usuarios.internal.core.gateway.HashSenhaGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.PublicarUsuarioAlteradoGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.RedefinirSenhaGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.RevogarSessoesGateway;

public final class RedefinirSenha implements RedefinirSenhaUseCase {
    private final RedefinirSenhaGateway usuarios;
    private final HashSenhaGateway hashes;
    private final PublicarUsuarioAlteradoGateway eventos;
    private final RevogarSessoesGateway sessoes;
    private final Clock clock;

    public RedefinirSenha(RedefinirSenhaGateway usuarios, HashSenhaGateway hashes,
            PublicarUsuarioAlteradoGateway eventos, RevogarSessoesGateway sessoes, Clock clock) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.hashes = Objects.requireNonNull(hashes);
        this.eventos = Objects.requireNonNull(eventos);
        this.sessoes = Objects.requireNonNull(sessoes);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override public void execute(UUID usuarioId, RedefinirSenhaCommand command) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(command, "command");
        var agora = clock.instant();
        var tipoPessoa = usuarios.redefinir(usuarioId, hashes.gerar(command.novaSenha()), agora);
        sessoes.revogarTodas(usuarioId, agora);
        eventos.publicar(new UsuarioAlterado(UUID.randomUUID(), usuarioId, tipoPessoa,
                UsuarioAlterado.Operacao.ATUALIZADO, agora));
    }
}
