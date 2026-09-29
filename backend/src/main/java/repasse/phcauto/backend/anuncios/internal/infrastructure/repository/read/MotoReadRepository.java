package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.read;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.MotoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface MotoReadRepository extends ReadOnlyRepository<MotoEntity, UUID> {
}
