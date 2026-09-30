package repasse.phcauto.backend.planos.internal.core.usecase;

import repasse.phcauto.backend.planos.internal.core.domain.NovoPlano;
import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;

public interface CriarPlanoUseCase { PlanoDados execute(NovoPlano plano); }
