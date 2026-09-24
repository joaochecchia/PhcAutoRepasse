package repasse.phcauto.backend.domain.usecases.identidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.Usuario;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Usuario.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractUsuarioCrudUseCase implements
        CriarUseCase<Usuario>,
        BuscarPorIdUseCase<Usuario, UUID>,
        AtualizarUseCase<Usuario, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Usuario> {

    @Override
    public abstract Usuario criar(Usuario dados);

    @Override
    public abstract Optional<Usuario> buscarPorId(UUID id);

    @Override
    public abstract Usuario atualizar(UUID id, Usuario dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Usuario> listar(int offset, int limite);
}
