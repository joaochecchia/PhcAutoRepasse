package repasse.phcauto.backend.usuarios.internal.infrastructure.configuration;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.UsuarioDetailsService;
@Configuration(proxyBeanMethods = false)
public class AutenticacaoConfiguration {
    @Bean public AuthenticationManager authenticationManager(UsuarioDetailsService details, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(details);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}
