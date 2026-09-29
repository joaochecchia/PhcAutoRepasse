package repasse.phcauto.backend.planos.internal.core;

import java.util.UUID;

public interface AtualizarPlanoUseCase { PlanoDados execute(UUID id, PlanoPatch patch); }
