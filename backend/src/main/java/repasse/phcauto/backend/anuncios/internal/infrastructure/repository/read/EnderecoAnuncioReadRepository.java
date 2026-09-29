package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.EnderecoAnuncioEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;

public interface EnderecoAnuncioReadRepository extends ReadOnlyRepository<EnderecoAnuncioEntity, UUID> {
}
