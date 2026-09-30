package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.read;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.VeiculoCaracteristicaEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.VeiculoCaracteristicaJpaId;

public interface VeiculoCaracteristicaReadRepository extends ReadOnlyRepository<VeiculoCaracteristicaEntity, VeiculoCaracteristicaJpaId> {
}
