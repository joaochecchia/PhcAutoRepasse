package repasse.phcauto.backend.usuarios.internal.infrastructure;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.core.AutenticacaoGateway;
import repasse.phcauto.backend.usuarios.internal.core.CredenciaisInvalidasException;
import repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write.UsuarioWriteRepository;

@Component
public class DatabaseAutenticacaoGateway implements AutenticacaoGateway {
    private final UsuarioWriteRepository usuarios;
    private final PasswordEncoder senhas;
    private final String hashSimulado;

    public DatabaseAutenticacaoGateway(UsuarioWriteRepository usuarios, PasswordEncoder senhas) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.hashSimulado = senhas.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public void autenticar(String email, String senha) {
        var candidatos = usuarios.findTop2ByEmailIgnoreCase(email);
        var usuario = candidatos.size() == 1 ? candidatos.get(0) : null;
        String hash = usuario == null ? null : usuario.getSenhaHash();
        boolean confere;
        try {
            confere = senhas.matches(senha, hash == null ? hashSimulado : hash);
        } catch (IllegalArgumentException hashIncompativel) {
            senhas.matches(senha, hashSimulado);
            throw new CredenciaisInvalidasException();
        }
        if (usuario == null || !usuario.getAtivo() || hash == null || !confere) {
            throw new CredenciaisInvalidasException();
        }
    }
}
