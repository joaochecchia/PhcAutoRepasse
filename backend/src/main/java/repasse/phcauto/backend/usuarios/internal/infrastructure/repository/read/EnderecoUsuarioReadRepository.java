package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.EnderecoUsuarioEntity;

public interface EnderecoUsuarioReadRepository extends ReadOnlyRepository<EnderecoUsuarioEntity, UUID> {
}
