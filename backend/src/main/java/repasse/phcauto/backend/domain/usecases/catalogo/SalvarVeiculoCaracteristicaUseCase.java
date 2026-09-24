package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.VeiculoCaracteristica;

/** Associa ou atualiza uma característica do veículo, preservando a chave composta. */
public interface SalvarVeiculoCaracteristicaUseCase {

    VeiculoCaracteristica executar(VeiculoCaracteristica veiculoCaracteristica);
}
