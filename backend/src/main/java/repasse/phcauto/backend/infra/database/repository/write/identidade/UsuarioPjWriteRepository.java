package repasse.phcauto.backend.infra.database.repository.write.identidade;

import repasse.phcauto.backend.infra.database.entity.identidade.UsuarioPjEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioPjWriteRepository extends JpaRepository<UsuarioPjEntity, UUID> {
}
