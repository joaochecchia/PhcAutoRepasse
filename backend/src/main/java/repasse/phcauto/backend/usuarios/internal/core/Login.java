package repasse.phcauto.backend.usuarios.internal.core;

import java.util.Objects;
import repasse.phcauto.backend.usuarios.request.LoginRequest;

public final class Login implements LoginUseCase {
    private final AutenticacaoGateway autenticacao;

    public Login(AutenticacaoGateway autenticacao) {
        this.autenticacao = Objects.requireNonNull(autenticacao);
    }

    @Override public void execute(LoginRequest request) {
        if (request == null) throw new IllegalArgumentException("Credenciais obrigatórias");
        autenticacao.autenticar(request.email(), request.senha());
    }
}
