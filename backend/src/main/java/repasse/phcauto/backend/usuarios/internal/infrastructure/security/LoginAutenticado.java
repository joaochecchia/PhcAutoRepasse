package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.util.UUID;
public record LoginAutenticado(UUID usuarioId, String papel, String token, long expiresIn) {
    @Override public String toString() { return "LoginAutenticado[token protegido]"; }
}
