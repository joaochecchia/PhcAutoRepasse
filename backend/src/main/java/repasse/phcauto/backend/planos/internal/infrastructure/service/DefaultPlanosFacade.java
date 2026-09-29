package repasse.phcauto.backend.planos.internal.infrastructure.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.planos.*;
import repasse.phcauto.backend.planos.internal.core.*;

@Service
public class DefaultPlanosFacade implements PlanosFacade {
    private final CriarPlanoUseCase criar;
    private final BuscarPlanoUseCase buscar;
    private final ListarPlanosUseCase listar;
    private final AtualizarPlanoUseCase atualizar;
    private final ExcluirPlanoUseCase excluir;

    public DefaultPlanosFacade(CriarPlanoUseCase criar, BuscarPlanoUseCase buscar,
            ListarPlanosUseCase listar, AtualizarPlanoUseCase atualizar, ExcluirPlanoUseCase excluir) {
        this.criar = criar; this.buscar = buscar; this.listar = listar;
        this.atualizar = atualizar; this.excluir = excluir;
    }

    @Override @Transactional(transactionManager = "writeTransactionManager")
    public PlanoResponse criar(CriarPlanoRequest request) {
        return response(criar.execute(new NovoPlano(request.nome(), request.valorCentavos(),
                request.periodoMeses(), request.limiteAnuncios(),
                request.limiteVistoriasCautelares(), request.ativo())));
    }
    @Override @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public PlanoResponse buscar(UUID id) { return response(buscar.execute(id)); }
    @Override @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public PlanoListaResponse listar(int offset, int limite) {
        var resultado = listar.execute(offset, limite);
        return new PlanoListaResponse(resultado.planos().stream().map(this::response).toList(),
                resultado.total(), resultado.offset(), resultado.limite());
    }
    @Override @Transactional(transactionManager = "writeTransactionManager")
    public PlanoResponse atualizar(UUID id, AtualizarPlanoRequest request) {
        if (request == null) throw new PlanoInvalidoException("Dados da alteração são obrigatórios");
        return response(atualizar.execute(id, new PlanoPatch(request.nome(), request.valorCentavos(),
                request.periodoMeses(), request.limiteAnuncios(),
                request.limiteVistoriasCautelares(), request.ativo())));
    }
    @Override @Transactional(transactionManager = "writeTransactionManager")
    public void excluir(UUID id) { excluir.execute(id); }

    private PlanoResponse response(PlanoDados plano) {
        return new PlanoResponse(plano.id(), plano.nome(), plano.valorCentavos(), plano.periodoMeses(),
                plano.limiteAnuncios(), plano.limiteVistoriasCautelares(), plano.ativo(), plano.criadoEm());
    }
}
