package repasse.phcauto.backend.infra.database.repository.read.assinaturas;

import repasse.phcauto.backend.infra.database.entity.assinaturas.AssinaturaEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface AssinaturaReadRepository extends ReadOnlyRepository<AssinaturaEntity, UUID> {
}
