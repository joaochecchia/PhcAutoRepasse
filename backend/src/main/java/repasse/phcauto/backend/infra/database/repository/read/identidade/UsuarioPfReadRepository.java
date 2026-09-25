package repasse.phcauto.backend.infra.database.repository.read.identidade;

import repasse.phcauto.backend.infra.database.entity.identidade.UsuarioPfEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface UsuarioPfReadRepository extends ReadOnlyRepository<UsuarioPfEntity, UUID> {
}
