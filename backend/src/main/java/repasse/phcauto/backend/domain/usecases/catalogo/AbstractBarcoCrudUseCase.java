package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Barco;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Barco.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractBarcoCrudUseCase implements
        CriarUseCase<Barco>,
        BuscarPorIdUseCase<Barco, UUID>,
        AtualizarUseCase<Barco, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Barco> {

    @Override
    public abstract Barco criar(Barco dados);

    @Override
    public abstract Optional<Barco> buscarPorId(UUID id);

    @Override
    public abstract Barco atualizar(UUID id, Barco dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Barco> listar(int offset, int limite);
}
