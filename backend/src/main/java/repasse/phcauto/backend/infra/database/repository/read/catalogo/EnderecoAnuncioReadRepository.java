package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.entity.catalogo.EnderecoAnuncioEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;

public interface EnderecoAnuncioReadRepository extends ReadOnlyRepository<EnderecoAnuncioEntity, UUID> {
}
