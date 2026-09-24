package repasse.phcauto.backend.domain.usecases.vendas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.vendas.Compra;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Compra.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractCompraCrudUseCase implements
        CriarUseCase<Compra>,
        BuscarPorIdUseCase<Compra, UUID>,
        AtualizarUseCase<Compra, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Compra> {

    @Override
    public abstract Compra criar(Compra dados);

    @Override
    public abstract Optional<Compra> buscarPorId(UUID id);

    @Override
    public abstract Compra atualizar(UUID id, Compra dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Compra> listar(int offset, int limite);
}
