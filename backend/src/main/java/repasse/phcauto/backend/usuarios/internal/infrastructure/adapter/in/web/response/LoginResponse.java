package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response;
public record LoginResponse(String mensagem, String accessToken, String tokenType, long expiresIn) {
    @Override public String toString() { return "LoginResponse[token protegido]"; }
}
