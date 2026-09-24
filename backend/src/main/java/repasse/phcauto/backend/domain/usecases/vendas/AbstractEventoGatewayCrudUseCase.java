package repasse.phcauto.backend.domain.usecases.vendas;

import java.util.List;
import java.util.Optional;
import repasse.phcauto.backend.domain.model.vendas.EventoGateway;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de EventoGateway.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractEventoGatewayCrudUseCase implements
        CriarUseCase<EventoGateway>,
        BuscarPorIdUseCase<EventoGateway, Long>,
        AtualizarUseCase<EventoGateway, Long>,
        ExcluirUseCase<Long>,
        ListarUseCase<EventoGateway> {

    @Override
    public abstract EventoGateway criar(EventoGateway dados);

    @Override
    public abstract Optional<EventoGateway> buscarPorId(Long id);

    @Override
    public abstract EventoGateway atualizar(Long id, EventoGateway dados);

    @Override
    public abstract boolean excluirPorId(Long id);

    @Override
    public abstract List<EventoGateway> listar(int offset, int limite);
}
