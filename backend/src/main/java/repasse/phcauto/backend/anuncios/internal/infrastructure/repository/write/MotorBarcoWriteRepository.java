package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.MotorBarcoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MotorBarcoWriteRepository extends JpaRepository<MotorBarcoEntity, UUID> {
    java.util.List<MotorBarcoEntity> findByBarcoId(UUID barcoId);
}
