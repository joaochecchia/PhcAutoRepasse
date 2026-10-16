package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.usuarios.SessoesUsuarios;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtLoginService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.RefreshTokenService;

@Service
class DefaultSessoesUsuarios implements SessoesUsuarios {
    private final JwtLoginService jwt;
    private final RefreshTokenService refresh;

    DefaultSessoesUsuarios(JwtLoginService jwt, RefreshTokenService refresh) {
        this.jwt = jwt;
        this.refresh = refresh;
    }

    @Override public SessaoEmitida iniciar(UUID usuarioId, String papel) {
        var access = jwt.emitir(usuarioId, papel);
        var renovacao = refresh.iniciar(usuarioId);
        return new SessaoEmitida(access.token(), access.expiresIn(), renovacao.token(), renovacao.expiresIn());
    }
}
