package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;

import java.util.UUID;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioAutenticado;
import repasse.phcauto.backend.usuarios.internal.core.exception.CredenciaisInvalidasException;
import repasse.phcauto.backend.usuarios.internal.core.gateway.AutenticacaoGateway;
@Component
public class DatabaseAutenticacaoGateway implements AutenticacaoGateway {
    private final AuthenticationManager authenticationManager;
    public DatabaseAutenticacaoGateway(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    @Override
    public UsuarioAutenticado autenticar(String email, String senha) {
        try {
            var auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, senha));
            var papel = auth.getAuthorities().iterator().next().getAuthority().substring(5);
            return new UsuarioAutenticado(UUID.fromString(auth.getName()), papel);
        } catch (AuthenticationException | IllegalArgumentException error) {
            throw new CredenciaisInvalidasException();
        }
    }
}
