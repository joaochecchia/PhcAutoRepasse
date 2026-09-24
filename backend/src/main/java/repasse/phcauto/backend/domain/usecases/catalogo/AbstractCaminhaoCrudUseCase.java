package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Caminhao;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Caminhao.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractCaminhaoCrudUseCase implements
        CriarUseCase<Caminhao>,
        BuscarPorIdUseCase<Caminhao, UUID>,
        AtualizarUseCase<Caminhao, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Caminhao> {

    @Override
    public abstract Caminhao criar(Caminhao dados);

    @Override
    public abstract Optional<Caminhao> buscarPorId(UUID id);

    @Override
    public abstract Caminhao atualizar(UUID id, Caminhao dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Caminhao> listar(int offset, int limite);
}
