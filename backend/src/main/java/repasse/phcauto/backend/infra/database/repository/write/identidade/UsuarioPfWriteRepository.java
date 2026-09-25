package repasse.phcauto.backend.infra.database.repository.write.identidade;

import repasse.phcauto.backend.infra.database.entity.identidade.UsuarioPfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioPfWriteRepository extends JpaRepository<UsuarioPfEntity, UUID> {
}
