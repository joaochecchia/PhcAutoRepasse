package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Barco;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface SalvarBarcoUseCase {

    Barco executar(Barco detalhes);
}
