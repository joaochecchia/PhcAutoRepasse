package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.EnderecoAnuncioEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;

public interface EnderecoAnuncioReadRepository extends ReadOnlyRepository<EnderecoAnuncioEntity, UUID> {
}
