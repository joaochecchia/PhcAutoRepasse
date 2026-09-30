package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.read;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.CaminhoneteEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface CaminhoneteReadRepository extends ReadOnlyRepository<CaminhoneteEntity, UUID> {
}
