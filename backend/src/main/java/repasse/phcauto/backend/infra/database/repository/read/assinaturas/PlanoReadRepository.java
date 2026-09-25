package repasse.phcauto.backend.infra.database.repository.read.assinaturas;

import repasse.phcauto.backend.infra.database.entity.assinaturas.PlanoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface PlanoReadRepository extends ReadOnlyRepository<PlanoEntity, UUID> {
}
