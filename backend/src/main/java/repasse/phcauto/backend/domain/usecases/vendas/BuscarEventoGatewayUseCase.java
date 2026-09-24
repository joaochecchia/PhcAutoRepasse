package repasse.phcauto.backend.domain.usecases.vendas;

import repasse.phcauto.backend.domain.model.vendas.EventoGateway;
import java.util.Optional;

/** Busca pela chave externa composta; retorna vazio quando não encontrado. */
public interface BuscarEventoGatewayUseCase {

    Optional<EventoGateway> executar(String provedor, String eventoExternoId);
}
