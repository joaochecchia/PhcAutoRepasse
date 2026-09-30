package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.util.Objects;
import repasse.phcauto.backend.usuarios.internal.core.domain.LoginCommand;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioAutenticado;
import repasse.phcauto.backend.usuarios.internal.core.gateway.AutenticacaoGateway;
public final class Login implements LoginUseCase {
    private final AutenticacaoGateway autenticacao;

    public Login(AutenticacaoGateway autenticacao) {
        this.autenticacao = Objects.requireNonNull(autenticacao);
    }

    @Override public UsuarioAutenticado execute(LoginCommand request) {
        if (request == null) throw new IllegalArgumentException("Credenciais obrigatórias");
        return autenticacao.autenticar(request.email(), request.senha());
    }
}
