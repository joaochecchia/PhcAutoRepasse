package repasse.phcauto.backend.planos;

import java.util.UUID;

public interface PlanosFacade {
    PlanoResponse criar(CriarPlanoRequest request);
    PlanoResponse buscar(UUID planoId);
    PlanoListaResponse listar(int offset, int limite);
    PlanoResponse atualizar(UUID planoId, AtualizarPlanoRequest request);
    void excluir(UUID planoId);
}
