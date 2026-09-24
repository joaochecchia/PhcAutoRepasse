package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.LinhaAmarela;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de LinhaAmarela.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractLinhaAmarelaCrudUseCase implements
        CriarUseCase<LinhaAmarela>,
        BuscarPorIdUseCase<LinhaAmarela, UUID>,
        AtualizarUseCase<LinhaAmarela, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<LinhaAmarela> {

    @Override
    public abstract LinhaAmarela criar(LinhaAmarela dados);

    @Override
    public abstract Optional<LinhaAmarela> buscarPorId(UUID id);

    @Override
    public abstract LinhaAmarela atualizar(UUID id, LinhaAmarela dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<LinhaAmarela> listar(int offset, int limite);
}
