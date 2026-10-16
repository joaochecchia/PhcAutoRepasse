package repasse.phcauto.backend.anuncios.internal.core.usecase;
import repasse.phcauto.backend.anuncios.internal.core.domain.*;
import repasse.phcauto.backend.domain.model.catalogo.FipeVeiculo;
public interface ReconciliarFipeVeiculoUseCase {
    CriarAnuncioCommand execute(CriarAnuncioCommand anterior,CriarAnuncioCommand atualizado,
            AtualizarAnuncioCommand patch,FipeVeiculo snapshot);
}
