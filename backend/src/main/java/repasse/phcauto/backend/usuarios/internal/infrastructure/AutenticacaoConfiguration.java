package repasse.phcauto.backend.usuarios.internal.infrastructure;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration(proxyBeanMethods = false)
class AutenticacaoConfiguration {
    @Bean AuthenticationManager authenticationManager(UsuarioDetailsService details, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(details);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}
