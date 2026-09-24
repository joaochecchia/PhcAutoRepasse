package repasse.phcauto.backend.domain.usecases.identidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.UsuarioPj;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de UsuarioPj.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractUsuarioPjCrudUseCase implements
        CriarUseCase<UsuarioPj>,
        BuscarPorIdUseCase<UsuarioPj, UUID>,
        AtualizarUseCase<UsuarioPj, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<UsuarioPj> {

    @Override
    public abstract UsuarioPj criar(UsuarioPj dados);

    @Override
    public abstract Optional<UsuarioPj> buscarPorId(UUID id);

    @Override
    public abstract UsuarioPj atualizar(UUID id, UsuarioPj dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<UsuarioPj> listar(int offset, int limite);
}
