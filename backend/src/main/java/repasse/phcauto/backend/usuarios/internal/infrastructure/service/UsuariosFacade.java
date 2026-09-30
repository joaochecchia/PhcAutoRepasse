package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.UsuarioDetalhadoResponse;

import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.UsuarioResponse;
/** API pública do módulo de usuários. */
public interface UsuariosFacade {
    UsuarioResponse criar(CriarUsuarioRequest request, String enderecoRede);
    UsuarioDetalhadoResponse buscar(UUID usuarioId);
    UsuarioDetalhadoResponse atualizar(UUID usuarioId, AtualizarUsuarioRequest request);
    void excluir(UUID usuarioId);
}
