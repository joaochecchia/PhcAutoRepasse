package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Caracteristica;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Caracteristica.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractCaracteristicaCrudUseCase implements
        CriarUseCase<Caracteristica>,
        BuscarPorIdUseCase<Caracteristica, UUID>,
        AtualizarUseCase<Caracteristica, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Caracteristica> {

    @Override
    public abstract Caracteristica criar(Caracteristica dados);

    @Override
    public abstract Optional<Caracteristica> buscarPorId(UUID id);

    @Override
    public abstract Caracteristica atualizar(UUID id, Caracteristica dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Caracteristica> listar(int offset, int limite);
}
