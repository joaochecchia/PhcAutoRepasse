package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import repasse.phcauto.backend.usuarios.SegurancaUsuarios;
import repasse.phcauto.backend.usuarios.UsuarioSeguranca;
import repasse.phcauto.backend.usuarios.CredencialInvalidaException;
import repasse.phcauto.backend.usuarios.FalhaAutenticacaoException;
import repasse.phcauto.backend.usuarios.internal.core.domain.LoginCommand;
import repasse.phcauto.backend.usuarios.internal.core.domain.RedefinirSenhaCommand;
import repasse.phcauto.backend.usuarios.internal.core.exception.CredenciaisInvalidasException;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.core.usecase.LoginUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.RedefinirSenhaUseCase;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;

@Service
class DefaultSegurancaUsuarios implements SegurancaUsuarios {
    private final LoginUseCase login;
    private final RedefinirSenhaUseCase redefinirSenha;
    private final UsuarioWriteRepository usuarios;
    private final TransactionTemplate transacao;
    private final Clock clock;

    DefaultSegurancaUsuarios(LoginUseCase login, RedefinirSenhaUseCase redefinirSenha,
            UsuarioWriteRepository usuarios,
            @Qualifier("writeTransactionManager") PlatformTransactionManager manager, Clock clock) {
        this.login = login;
        this.redefinirSenha = redefinirSenha;
        this.usuarios = usuarios;
        this.transacao = new TransactionTemplate(manager);
        this.clock = clock;
    }

    @Override public UsuarioSeguranca autenticar(String email, String senha) {
        try {
            var autenticado = login.execute(new LoginCommand(email, senha));
            return buscarAtivo(autenticado.id());
        } catch (CredenciaisInvalidasException error) {
            throw new FalhaAutenticacaoException();
        }
    }

    @Override public Optional<UsuarioSeguranca> buscarAtivoPorEmail(String email) {
        if (email == null) return Optional.empty();
        var encontrados = usuarios.findTop2ByEmailIgnoreCase(email.strip().toLowerCase(Locale.ROOT));
        if (encontrados.size() != 1 || !encontrados.get(0).getAtivo()) return Optional.empty();
        return Optional.of(mapear(encontrados.get(0)));
    }

    @Override public UsuarioSeguranca buscarAtivo(UUID usuarioId) {
        return usuarios.findById(usuarioId).filter(u -> u.getAtivo()).map(DefaultSegurancaUsuarios::mapear)
                .orElseThrow(FalhaAutenticacaoException::new);
    }

    @Override public void redefinirSenha(UUID usuarioId, String novaSenha) {
        try {
            transacao.executeWithoutResult(status -> redefinirSenha.execute(usuarioId, new RedefinirSenhaCommand(novaSenha)));
        } catch (CadastroInvalidoException error) {
            throw new CredencialInvalidaException(error.getMessage());
        }
    }

    @Override public void desabilitarSenhaLocal(UUID usuarioId) {
        transacao.executeWithoutResult(status -> {
            var usuario = usuarios.findById(usuarioId).filter(u -> u.getAtivo())
                    .orElseThrow(FalhaAutenticacaoException::new);
            usuario.desabilitarSenhaLocal(clock.instant());
            usuarios.saveAndFlush(usuario);
        });
    }

    private static UsuarioSeguranca mapear(
            repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity usuario) {
        return new UsuarioSeguranca(usuario.getId(), usuario.getEmail(), usuario.getPapel().name(),
                usuario.getSessaoVersao(), usuario.possuiSenhaLocal());
    }
}
