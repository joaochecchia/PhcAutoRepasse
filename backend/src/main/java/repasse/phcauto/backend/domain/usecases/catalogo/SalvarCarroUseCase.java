package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Carro;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface SalvarCarroUseCase {

    Carro executar(Carro detalhes);
}
