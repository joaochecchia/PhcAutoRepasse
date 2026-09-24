package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Anuncio;

/**
 * Contrato de gravação; retorna o modelo salvo.
 * A implementação deve validar as regras e a autorização aplicáveis antes de persistir.
 * Não implica publicação, ativação ou confirmação de pagamento.
 */
public interface SalvarAnuncioUseCase {

    Anuncio executar(Anuncio anuncio);
}
