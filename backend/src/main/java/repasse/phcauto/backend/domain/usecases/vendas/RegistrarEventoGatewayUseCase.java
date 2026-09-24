package repasse.phcauto.backend.domain.usecases.vendas;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra o recebimento de um evento, com identificador interno gerado pela implementação.
 * A combinação provedor/eventoExternoId deve ser única, inclusive sob concorrência.
 * Retorna true para um novo registro e false para um evento já registrado.
 * Registrar recebimento não significa concluir o processamento ou confirmar pagamento.
 */
public interface RegistrarEventoGatewayUseCase {

    boolean executar(String provedor, String eventoExternoId, UUID pagamentoId, Instant recebidoEm);
}
