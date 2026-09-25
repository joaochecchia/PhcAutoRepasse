package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.BarcoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface BarcoReadRepository extends ReadOnlyRepository<BarcoEntity, UUID> {
}
