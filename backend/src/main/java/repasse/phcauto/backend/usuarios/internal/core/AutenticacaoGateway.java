package repasse.phcauto.backend.usuarios.internal.core;

public interface AutenticacaoGateway {
    void autenticar(String email, String senha);
}
