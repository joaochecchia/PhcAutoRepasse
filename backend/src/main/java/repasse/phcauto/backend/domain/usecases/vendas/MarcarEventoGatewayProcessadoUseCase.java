package repasse.phcauto.backend.domain.usecases.vendas;

import java.time.Instant;

/**
 * Registra a conclusão do processamento de um evento existente.
 * Deve ser coordenado atomicamente com os efeitos do processamento pela implementação.
 * Repetições não devem sobrescrever a data de conclusão já registrada.
 * Evento inexistente deve resultar em erro, sem criar um novo evento.
 */
public interface MarcarEventoGatewayProcessadoUseCase {

    void executar(long eventoId, Instant processadoEm);
}
