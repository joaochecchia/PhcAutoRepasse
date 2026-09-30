package repasse.phcauto.backend.usuarios.response;
public record LoginResponse(String mensagem, String accessToken, String tokenType, long expiresIn) {
    @Override public String toString() { return "LoginResponse[token protegido]"; }
}
