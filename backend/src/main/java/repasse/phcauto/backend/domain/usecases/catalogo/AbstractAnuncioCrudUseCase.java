package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Anuncio;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Anuncio.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractAnuncioCrudUseCase implements
        CriarUseCase<Anuncio>,
        BuscarPorIdUseCase<Anuncio, UUID>,
        AtualizarUseCase<Anuncio, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Anuncio> {

    @Override
    public abstract Anuncio criar(Anuncio dados);

    @Override
    public abstract Optional<Anuncio> buscarPorId(UUID id);

    @Override
    public abstract Anuncio atualizar(UUID id, Anuncio dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Anuncio> listar(int offset, int limite);
}
