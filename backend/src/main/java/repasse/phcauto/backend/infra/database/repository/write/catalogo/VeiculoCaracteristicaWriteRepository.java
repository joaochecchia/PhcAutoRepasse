package repasse.phcauto.backend.infra.database.repository.write.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoCaracteristicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoCaracteristicaJpaId;

public interface VeiculoCaracteristicaWriteRepository extends JpaRepository<VeiculoCaracteristicaEntity, VeiculoCaracteristicaJpaId> {
}
