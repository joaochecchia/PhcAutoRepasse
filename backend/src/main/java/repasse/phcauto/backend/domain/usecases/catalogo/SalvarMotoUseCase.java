package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Moto;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface SalvarMotoUseCase {

    Moto executar(Moto detalhes);
}
