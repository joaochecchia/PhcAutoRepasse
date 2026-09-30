package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.VeiculoCaracteristicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.VeiculoCaracteristicaJpaId;

public interface VeiculoCaracteristicaWriteRepository extends JpaRepository<VeiculoCaracteristicaEntity, VeiculoCaracteristicaJpaId> {
    java.util.List<VeiculoCaracteristicaEntity> findByIdVeiculoId(java.util.UUID veiculoId);
}
