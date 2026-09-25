package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.CaminhoneteEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface CaminhoneteReadRepository extends ReadOnlyRepository<CaminhoneteEntity, UUID> {
}
