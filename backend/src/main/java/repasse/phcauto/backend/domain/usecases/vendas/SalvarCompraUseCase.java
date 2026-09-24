package repasse.phcauto.backend.domain.usecases.vendas;

import repasse.phcauto.backend.domain.model.vendas.Compra;

/**
 * Contrato de gravação; retorna o modelo salvo.
 * A implementação deve validar as regras e a autorização aplicáveis antes de persistir.
 * Não implica publicação, ativação ou confirmação de pagamento.
 */
public interface SalvarCompraUseCase {

    Compra executar(Compra compra);
}
