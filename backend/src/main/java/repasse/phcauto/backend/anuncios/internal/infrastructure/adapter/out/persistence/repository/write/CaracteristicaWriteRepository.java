package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.CaracteristicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CaracteristicaWriteRepository extends JpaRepository<CaracteristicaEntity, UUID> {
}
