package repasse.phcauto.backend.infra.database.repository.write.assinaturas;

import repasse.phcauto.backend.infra.database.entity.assinaturas.PlanoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlanoWriteRepository extends JpaRepository<PlanoEntity, UUID> {
}
