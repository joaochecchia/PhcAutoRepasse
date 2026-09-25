package repasse.phcauto.backend.usuarios.internal.infrastructure;

import java.time.Clock;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import repasse.phcauto.backend.usuarios.internal.core.*;

@Configuration(proxyBeanMethods = false)
class UsuariosConfiguration {
    @Bean Clock usuariosClock() { return Clock.systemUTC(); }

    @Bean PasswordEncoder usuarioPasswordEncoder() {
        // TODO: ao adotar BCryptPasswordEncoder para novos hashes, manter PBKDF2 para contas existentes.
        return new DelegatingPasswordEncoder("pbkdf2@SpringSecurity_v5_8", Map.of(
                "pbkdf2@SpringSecurity_v5_8", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
                "bcrypt", new BCryptPasswordEncoder()));
    }

    @Bean HashSenhaGateway hashSenhaGateway(PasswordEncoder encoder) { return encoder::encode; }

    @Bean LoginUseCase loginUseCase(AutenticacaoGateway autenticacao) { return new Login(autenticacao); }

    @Bean CriarUsuarioUseCase criarUsuarioUseCase(UsuarioGateway usuarios, HashSenhaGateway senhas,
            PublicarUsuarioCriadoGateway eventos, Clock usuariosClock) {
        return new CriarUsuario(usuarios, senhas, eventos, usuariosClock);
    }

    @Bean BuscarUsuarioUseCase buscarUsuarioUseCase(ConsultarUsuarioGateway usuarios) {
        return new BuscarUsuario(usuarios);
    }

    @Bean AtualizarUsuarioUseCase atualizarUsuarioUseCase(ConsultarUsuarioGateway consultas,
            AtualizarUsuarioGateway atualizacoes, HashSenhaGateway senhas,
            PublicarUsuarioAlteradoGateway eventos, Clock usuariosClock) {
        return new AtualizarUsuario(consultas, atualizacoes, senhas, eventos, usuariosClock);
    }

    @Bean ExcluirUsuarioUseCase excluirUsuarioUseCase(ExcluirUsuarioGateway usuarios,
            PublicarUsuarioAlteradoGateway eventos, Clock usuariosClock) {
        return new ExcluirUsuario(usuarios, eventos, usuariosClock);
    }
}
