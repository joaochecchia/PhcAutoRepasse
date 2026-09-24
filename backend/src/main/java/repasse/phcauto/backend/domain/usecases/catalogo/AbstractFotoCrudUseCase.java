package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Foto;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Foto.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractFotoCrudUseCase implements
        CriarUseCase<Foto>,
        BuscarPorIdUseCase<Foto, UUID>,
        AtualizarUseCase<Foto, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Foto> {

    @Override
    public abstract Foto criar(Foto dados);

    @Override
    public abstract Optional<Foto> buscarPorId(UUID id);

    @Override
    public abstract Foto atualizar(UUID id, Foto dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Foto> listar(int offset, int limite);
}
