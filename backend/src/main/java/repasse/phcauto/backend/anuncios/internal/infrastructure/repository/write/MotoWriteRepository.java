package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.MotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MotoWriteRepository extends JpaRepository<MotoEntity, UUID> {
}
