package repasse.phcauto.backend.usuarios.internal.core;

public interface AutenticacaoGateway {
    UsuarioAutenticado autenticar(String email, String senha);
}
