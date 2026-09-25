package repasse.phcauto.backend.infra.database.repository.write.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.CaminhoneteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CaminhoneteWriteRepository extends JpaRepository<CaminhoneteEntity, UUID> {
}
