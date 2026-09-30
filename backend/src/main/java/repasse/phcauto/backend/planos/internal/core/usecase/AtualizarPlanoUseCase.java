package repasse.phcauto.backend.planos.internal.core.usecase;

import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
import repasse.phcauto.backend.planos.internal.core.domain.PlanoPatch;
public interface AtualizarPlanoUseCase { PlanoDados execute(UUID id, PlanoPatch patch); }
