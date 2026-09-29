package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.CaminhoneteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CaminhoneteWriteRepository extends JpaRepository<CaminhoneteEntity, UUID> {
}
