package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.read;

import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface UsuarioReadRepository extends ReadOnlyRepository<UsuarioEntity, UUID> {
}
