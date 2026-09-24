package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import repasse.phcauto.backend.domain.model.catalogo.VeiculoCaracteristica;
import repasse.phcauto.backend.domain.model.catalogo.VeiculoCaracteristicaId;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de VeiculoCaracteristica.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractVeiculoCaracteristicaCrudUseCase implements
        CriarUseCase<VeiculoCaracteristica>,
        BuscarPorIdUseCase<VeiculoCaracteristica, VeiculoCaracteristicaId>,
        AtualizarUseCase<VeiculoCaracteristica, VeiculoCaracteristicaId>,
        ExcluirUseCase<VeiculoCaracteristicaId>,
        ListarUseCase<VeiculoCaracteristica> {

    @Override
    public abstract VeiculoCaracteristica criar(VeiculoCaracteristica dados);

    @Override
    public abstract Optional<VeiculoCaracteristica> buscarPorId(VeiculoCaracteristicaId id);

    @Override
    public abstract VeiculoCaracteristica atualizar(VeiculoCaracteristicaId id, VeiculoCaracteristica dados);

    @Override
    public abstract boolean excluirPorId(VeiculoCaracteristicaId id);

    @Override
    public abstract List<VeiculoCaracteristica> listar(int offset, int limite);
}
