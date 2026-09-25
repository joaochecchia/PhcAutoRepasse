package repasse.phcauto.backend.infra.database.repository.read.identidade;

import repasse.phcauto.backend.infra.database.entity.identidade.UsuarioPjEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface UsuarioPjReadRepository extends ReadOnlyRepository<UsuarioPjEntity, UUID> {
}
