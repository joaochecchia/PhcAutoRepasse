package repasse.phcauto.backend.planos.internal.infrastructure.adapter.out.persistence.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.out.persistence.entity.PlanoEntity;

public interface PlanoReadRepository extends ReadOnlyRepository<PlanoEntity, UUID> { }
