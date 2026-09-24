package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.LinhaAmarela;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface SalvarLinhaAmarelaUseCase {

    LinhaAmarela executar(LinhaAmarela detalhes);
}
