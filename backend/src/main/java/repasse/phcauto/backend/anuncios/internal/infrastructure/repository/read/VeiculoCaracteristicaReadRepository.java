package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.read;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.VeiculoCaracteristicaEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.VeiculoCaracteristicaJpaId;

public interface VeiculoCaracteristicaReadRepository extends ReadOnlyRepository<VeiculoCaracteristicaEntity, VeiculoCaracteristicaJpaId> {
}
