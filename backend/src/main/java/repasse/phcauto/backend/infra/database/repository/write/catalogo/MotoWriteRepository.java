package repasse.phcauto.backend.infra.database.repository.write.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.MotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MotoWriteRepository extends JpaRepository<MotoEntity, UUID> {
}
