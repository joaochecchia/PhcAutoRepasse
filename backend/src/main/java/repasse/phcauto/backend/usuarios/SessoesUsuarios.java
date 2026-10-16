package repasse.phcauto.backend.usuarios;

import java.util.UUID;

public interface SessoesUsuarios {
    SessaoEmitida iniciar(UUID usuarioId, String papel);
    record SessaoEmitida(String accessToken, long accessExpiresIn, String refreshToken, long refreshExpiresIn) { }
}
