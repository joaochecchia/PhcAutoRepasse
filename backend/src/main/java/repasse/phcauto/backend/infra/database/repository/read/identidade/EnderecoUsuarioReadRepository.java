package repasse.phcauto.backend.infra.database.repository.read.identidade;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.infra.database.entity.identidade.EnderecoUsuarioEntity;

public interface EnderecoUsuarioReadRepository extends ReadOnlyRepository<EnderecoUsuarioEntity, UUID> {
}
