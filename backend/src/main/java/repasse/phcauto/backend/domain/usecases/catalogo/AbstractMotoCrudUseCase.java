package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Moto;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Moto.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractMotoCrudUseCase implements
        CriarUseCase<Moto>,
        BuscarPorIdUseCase<Moto, UUID>,
        AtualizarUseCase<Moto, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Moto> {

    @Override
    public abstract Moto criar(Moto dados);

    @Override
    public abstract Optional<Moto> buscarPorId(UUID id);

    @Override
    public abstract Moto atualizar(UUID id, Moto dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Moto> listar(int offset, int limite);
}
