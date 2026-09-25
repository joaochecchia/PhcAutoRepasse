package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.CaracteristicaEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface CaracteristicaReadRepository extends ReadOnlyRepository<CaracteristicaEntity, UUID> {
}
