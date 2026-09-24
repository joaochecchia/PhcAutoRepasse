package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Caminhonete;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface SalvarCaminhoneteUseCase {

    Caminhonete executar(Caminhonete detalhes);
}
