package repasse.phcauto.backend.usuarios.internal.infrastructure;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.usuarios.internal.core.AutenticacaoGateway;
import repasse.phcauto.backend.usuarios.internal.core.CredenciaisInvalidasException;

@Component
public class DatabaseAutenticacaoGateway implements AutenticacaoGateway {
    private final UsuarioCredenciaisReader credenciais;
    private final PasswordEncoder senhas;
    private final String hashSimulado;

    public DatabaseAutenticacaoGateway(UsuarioCredenciaisReader credenciais, PasswordEncoder senhas) {
        this.credenciais = credenciais;
        this.senhas = senhas;
        this.hashSimulado = senhas.encode(UUID.randomUUID().toString());
    }

    @Override
    public void autenticar(String email, String senha) {
        var credencial = credenciais.carregar(email);
        String hash = credencial.senhaHash();
        boolean confere;
        try {
            confere = senhas.matches(senha, hash == null ? hashSimulado : hash);
        } catch (IllegalArgumentException hashIncompativel) {
            senhas.matches(senha, hashSimulado);
            throw new CredenciaisInvalidasException();
        }
        if (!credencial.encontrada() || !credencial.ativa() || hash == null || !confere) {
            throw new CredenciaisInvalidasException();
        }
    }
}
