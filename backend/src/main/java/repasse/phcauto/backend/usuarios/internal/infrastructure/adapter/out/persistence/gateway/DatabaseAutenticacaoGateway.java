package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;

import java.util.UUID;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioAutenticado;
import repasse.phcauto.backend.usuarios.internal.core.exception.CredenciaisInvalidasException;
import repasse.phcauto.backend.usuarios.internal.core.gateway.AutenticacaoGateway;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.LoginAttemptLimiter;
@Component
public class DatabaseAutenticacaoGateway implements AutenticacaoGateway {
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptLimiter tentativas;
    public DatabaseAutenticacaoGateway(AuthenticationManager authenticationManager,
            LoginAttemptLimiter tentativas) {
        this.authenticationManager = authenticationManager;
        this.tentativas = tentativas;
    }
    @Override
    public UsuarioAutenticado autenticar(String email, String senha) {
        tentativas.antesDeAutenticar(email);
        try {
            var auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, senha));
            var papel = auth.getAuthorities().iterator().next().getAuthority().substring(5);
            tentativas.autenticacaoBemSucedida(email);
            return new UsuarioAutenticado(UUID.fromString(auth.getName()), papel);
        } catch (AuthenticationException | IllegalArgumentException error) {
            throw new CredenciaisInvalidasException();
        }
    }
}
