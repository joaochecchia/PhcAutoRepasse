package repasse.phcauto.backend.planos.internal.core.usecase;

import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
public interface BuscarPlanoUseCase { PlanoDados execute(UUID id); }
