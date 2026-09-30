package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.read;

import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioPfEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface UsuarioPfReadRepository extends ReadOnlyRepository<UsuarioPfEntity, UUID> {
}
