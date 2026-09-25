package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.FotoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface FotoReadRepository extends ReadOnlyRepository<FotoEntity, UUID> {
}
