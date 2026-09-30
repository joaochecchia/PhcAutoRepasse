package repasse.phcauto.backend.usuarios.internal.infrastructure;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import repasse.phcauto.backend.usuarios.internal.core.LoginUseCase;
import repasse.phcauto.backend.usuarios.request.LoginRequest;
import repasse.phcauto.backend.usuarios.response.LoginResponse;

@Service
public class JwtLoginService {
    private final LoginUseCase login;
    private final JwtEncoder encoder;
    private final Clock clock;
    private final String issuer;
    private final long ttl;
    public JwtLoginService(LoginUseCase login, JwtEncoder encoder, Clock applicationClock,
            @Value("${JWT_ISSUER:phcauto}") String issuer,
            @Value("${JWT_TTL_SECONDS:900}") long ttl) {
        if (ttl <= 0 || ttl > 86400) throw new IllegalArgumentException("JWT_TTL_SECONDS deve estar entre 1 e 86400");
        this.login = login; this.encoder = encoder; this.clock = applicationClock; this.issuer = issuer; this.ttl = ttl;
    }
    public LoginResponse execute(LoginRequest request) {
        var usuario = login.execute(request);
        var now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(usuario.id().toString())
                .issuedAt(now).expiresAt(now.plusSeconds(ttl)).claim("roles", java.util.List.of(usuario.papel())).build();
        var token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse("Login realizado com sucesso", token, "Bearer", ttl);
    }
}
