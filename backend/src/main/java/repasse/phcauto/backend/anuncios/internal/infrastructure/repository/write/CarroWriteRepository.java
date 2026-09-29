package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.CarroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CarroWriteRepository extends JpaRepository<CarroEntity, UUID> {
}
