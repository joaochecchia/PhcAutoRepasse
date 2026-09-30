package repasse.phcauto.backend.planos.internal.infrastructure.service;

import java.util.UUID;

import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.request.AtualizarPlanoRequest;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.request.CriarPlanoRequest;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.response.PlanoListaResponse;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.response.PlanoResponse;
public interface PlanosFacade {
    PlanoResponse criar(CriarPlanoRequest request);
    PlanoResponse buscar(UUID planoId);
    PlanoListaResponse listar(int offset, int limite);
    PlanoResponse atualizar(UUID planoId, AtualizarPlanoRequest request);
    void excluir(UUID planoId);
}
