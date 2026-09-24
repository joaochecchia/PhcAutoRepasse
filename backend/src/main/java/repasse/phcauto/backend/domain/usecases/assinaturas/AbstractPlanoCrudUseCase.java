package repasse.phcauto.backend.domain.usecases.assinaturas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.assinaturas.Plano;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Plano.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractPlanoCrudUseCase implements
        CriarUseCase<Plano>,
        BuscarPorIdUseCase<Plano, UUID>,
        AtualizarUseCase<Plano, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Plano> {

    @Override
    public abstract Plano criar(Plano dados);

    @Override
    public abstract Optional<Plano> buscarPorId(UUID id);

    @Override
    public abstract Plano atualizar(UUID id, Plano dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Plano> listar(int offset, int limite);
}
