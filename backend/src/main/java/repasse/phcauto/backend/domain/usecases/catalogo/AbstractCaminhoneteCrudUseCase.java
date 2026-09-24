package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Caminhonete;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Caminhonete.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractCaminhoneteCrudUseCase implements
        CriarUseCase<Caminhonete>,
        BuscarPorIdUseCase<Caminhonete, UUID>,
        AtualizarUseCase<Caminhonete, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Caminhonete> {

    @Override
    public abstract Caminhonete criar(Caminhonete dados);

    @Override
    public abstract Optional<Caminhonete> buscarPorId(UUID id);

    @Override
    public abstract Caminhonete atualizar(UUID id, Caminhonete dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Caminhonete> listar(int offset, int limite);
}
