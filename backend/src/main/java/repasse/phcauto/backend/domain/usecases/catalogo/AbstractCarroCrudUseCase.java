package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Carro;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Carro.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractCarroCrudUseCase implements
        CriarUseCase<Carro>,
        BuscarPorIdUseCase<Carro, UUID>,
        AtualizarUseCase<Carro, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Carro> {

    @Override
    public abstract Carro criar(Carro dados);

    @Override
    public abstract Optional<Carro> buscarPorId(UUID id);

    @Override
    public abstract Carro atualizar(UUID id, Carro dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Carro> listar(int offset, int limite);
}
