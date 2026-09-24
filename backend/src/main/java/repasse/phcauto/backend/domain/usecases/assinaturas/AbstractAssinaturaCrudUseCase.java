package repasse.phcauto.backend.domain.usecases.assinaturas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.assinaturas.Assinatura;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de Assinatura.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractAssinaturaCrudUseCase implements
        CriarUseCase<Assinatura>,
        BuscarPorIdUseCase<Assinatura, UUID>,
        AtualizarUseCase<Assinatura, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<Assinatura> {

    @Override
    public abstract Assinatura criar(Assinatura dados);

    @Override
    public abstract Optional<Assinatura> buscarPorId(UUID id);

    @Override
    public abstract Assinatura atualizar(UUID id, Assinatura dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<Assinatura> listar(int offset, int limite);
}
