package repasse.phcauto.backend.usuarios;

import java.util.Optional;
import java.util.UUID;

public interface SegurancaUsuarios {
    UsuarioSeguranca autenticar(String email, String senha);
    Optional<UsuarioSeguranca> buscarAtivoPorEmail(String email);
    UsuarioSeguranca buscarAtivo(UUID usuarioId);
    void redefinirSenha(UUID usuarioId, String novaSenha);
    void desabilitarSenhaLocal(UUID usuarioId);
}
