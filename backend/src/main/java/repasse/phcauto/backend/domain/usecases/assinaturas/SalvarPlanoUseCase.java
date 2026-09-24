package repasse.phcauto.backend.domain.usecases.assinaturas;

import repasse.phcauto.backend.domain.model.assinaturas.Plano;

/**
 * Contrato de gravação; retorna o modelo salvo.
 * A implementação deve validar as regras e a autorização aplicáveis antes de persistir.
 * Não implica publicação, ativação ou confirmação de pagamento.
 */
public interface SalvarPlanoUseCase {

    Plano executar(Plano plano);
}
