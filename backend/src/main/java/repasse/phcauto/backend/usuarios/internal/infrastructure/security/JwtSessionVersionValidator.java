package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;

@Component
public class JwtSessionVersionValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error SESSAO_INVALIDA = new OAuth2Error(
            "invalid_token", "A sessão foi encerrada", null);
    private final UsuarioWriteRepository usuarios;

    public JwtSessionVersionValidator(UsuarioWriteRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        try {
            UUID usuarioId = UUID.fromString(token.getSubject());
            Long versaoToken = token.getClaim("sessionVersion");
            boolean valida = versaoToken != null && usuarios.findById(usuarioId)
                    .filter(usuario -> usuario.getAtivo() && usuario.getSessaoVersao() == versaoToken.longValue())
                    .isPresent();
            return valida ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(SESSAO_INVALIDA);
        } catch (IllegalArgumentException error) {
            return OAuth2TokenValidatorResult.failure(SESSAO_INVALIDA);
        }
    }
}
