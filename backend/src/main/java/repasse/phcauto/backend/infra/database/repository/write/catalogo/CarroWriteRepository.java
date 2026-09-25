package repasse.phcauto.backend.infra.database.repository.write.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.CarroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CarroWriteRepository extends JpaRepository<CarroEntity, UUID> {
}
