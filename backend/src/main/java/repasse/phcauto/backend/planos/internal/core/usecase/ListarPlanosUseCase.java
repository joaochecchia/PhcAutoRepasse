package repasse.phcauto.backend.planos.internal.core.usecase;

import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;

public interface ListarPlanosUseCase { Resultado execute(int offset, int limite);
    record Resultado(java.util.List<PlanoDados> planos, long total, int offset, int limite) { }
}
