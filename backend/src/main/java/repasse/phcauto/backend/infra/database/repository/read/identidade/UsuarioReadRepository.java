package repasse.phcauto.backend.infra.database.repository.read.identidade;

import repasse.phcauto.backend.infra.database.entity.identidade.UsuarioEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface UsuarioReadRepository extends ReadOnlyRepository<UsuarioEntity, UUID> {
}
