package repasse.phcauto.backend.infra.database.repository.write.identidade;

import repasse.phcauto.backend.infra.database.entity.identidade.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioWriteRepository extends JpaRepository<UsuarioEntity, UUID> {
}
