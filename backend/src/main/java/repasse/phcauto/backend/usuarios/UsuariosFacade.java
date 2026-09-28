package repasse.phcauto.backend.usuarios;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.response.UsuarioDetalhadoResponse;

/** API pública do módulo de usuários. */
public interface UsuariosFacade {
    UsuarioResponse criar(CriarUsuarioRequest request);
    UsuarioDetalhadoResponse buscar(UUID usuarioId);
    UsuarioDetalhadoResponse atualizar(UUID usuarioId, AtualizarUsuarioRequest request);
    void excluir(UUID usuarioId);
}
