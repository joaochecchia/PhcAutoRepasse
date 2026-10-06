package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response;

import java.util.UUID;

public record SessaoResponse(boolean autenticado, UUID usuarioId, String papel, long expiresIn) {
    public static SessaoResponse anonima() { return new SessaoResponse(false, null, null, 0); }
}
