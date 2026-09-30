package repasse.phcauto.backend.usuarios.internal.infrastructure.configuration;

import java.time.Clock;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import repasse.phcauto.backend.usuarios.internal.core.gateway.AtualizarUsuarioGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.AutenticacaoGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.ConsultarUsuarioGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.ExcluirUsuarioGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.HashSenhaGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.PublicarUsuarioAlteradoGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.PublicarUsuarioCriadoGateway;
import repasse.phcauto.backend.usuarios.internal.core.gateway.UsuarioGateway;
import repasse.phcauto.backend.usuarios.internal.core.usecase.AtualizarUsuario;
import repasse.phcauto.backend.usuarios.internal.core.usecase.AtualizarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.BuscarUsuario;
import repasse.phcauto.backend.usuarios.internal.core.usecase.BuscarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.CriarUsuario;
import repasse.phcauto.backend.usuarios.internal.core.usecase.CriarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.ExcluirUsuario;
import repasse.phcauto.backend.usuarios.internal.core.usecase.ExcluirUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.Login;
import repasse.phcauto.backend.usuarios.internal.core.usecase.LoginUseCase;
@Configuration(proxyBeanMethods = false)
public class UsuariosConfiguration {
    @Bean public PasswordEncoder usuarioPasswordEncoder() {
        var encoder = new DelegatingPasswordEncoder("bcrypt", Map.of(
                "pbkdf2@SpringSecurity_v5_8", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
                "bcrypt", new BCryptPasswordEncoder()));
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return encoder;
    }

    @Bean public HashSenhaGateway hashSenhaGateway(PasswordEncoder encoder) { return encoder::encode; }

    @Bean LoginUseCase loginUseCase(AutenticacaoGateway autenticacao) { return new Login(autenticacao); }

    @Bean CriarUsuarioUseCase criarUsuarioUseCase(UsuarioGateway usuarios, HashSenhaGateway senhas,
            PublicarUsuarioCriadoGateway eventos, Clock applicationClock) {
        return new CriarUsuario(usuarios, senhas, eventos, applicationClock);
    }

    @Bean BuscarUsuarioUseCase buscarUsuarioUseCase(ConsultarUsuarioGateway usuarios) {
        return new BuscarUsuario(usuarios);
    }

    @Bean AtualizarUsuarioUseCase atualizarUsuarioUseCase(ConsultarUsuarioGateway consultas,
            AtualizarUsuarioGateway atualizacoes, HashSenhaGateway senhas,
            PublicarUsuarioAlteradoGateway eventos, Clock applicationClock) {
        return new AtualizarUsuario(consultas, atualizacoes, senhas, eventos, applicationClock);
    }

    @Bean ExcluirUsuarioUseCase excluirUsuarioUseCase(ExcluirUsuarioGateway usuarios,
            PublicarUsuarioAlteradoGateway eventos, Clock applicationClock) {
        return new ExcluirUsuario(usuarios, eventos, applicationClock);
    }
}
