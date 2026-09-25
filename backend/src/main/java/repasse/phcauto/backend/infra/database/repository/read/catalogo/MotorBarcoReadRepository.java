package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.MotorBarcoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface MotorBarcoReadRepository extends ReadOnlyRepository<MotorBarcoEntity, UUID> {
}
