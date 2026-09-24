package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Veiculo;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Veiculo.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractVeiculoCrudUseCase implements
        CriarUseCase<Veiculo>,
        BuscarPorIdUseCase<Veiculo, UUID>,
        AtualizarUseCase<Veiculo, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Veiculo> {

    @Override
    public abstract Veiculo criar(Veiculo dados);

    @Override
    public abstract Optional<Veiculo> buscarPorId(UUID id);

    @Override
    public abstract Veiculo atualizar(UUID id, Veiculo dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Veiculo> listar(int offset, int limite);
}
