package repasse.phcauto.backend.domain.usecases.vendas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.vendas.Pagamento;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Pagamento.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractPagamentoCrudUseCase implements
        CriarUseCase<Pagamento>,
        BuscarPorIdUseCase<Pagamento, UUID>,
        AtualizarUseCase<Pagamento, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Pagamento> {

    @Override
    public abstract Pagamento criar(Pagamento dados);

    @Override
    public abstract Optional<Pagamento> buscarPorId(UUID id);

    @Override
    public abstract Pagamento atualizar(UUID id, Pagamento dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Pagamento> listar(int offset, int limite);
}
