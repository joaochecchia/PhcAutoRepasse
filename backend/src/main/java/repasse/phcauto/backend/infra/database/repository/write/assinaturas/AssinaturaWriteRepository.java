package repasse.phcauto.backend.infra.database.repository.write.assinaturas;

import repasse.phcauto.backend.infra.database.entity.assinaturas.AssinaturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AssinaturaWriteRepository extends JpaRepository<AssinaturaEntity, UUID> {
}
