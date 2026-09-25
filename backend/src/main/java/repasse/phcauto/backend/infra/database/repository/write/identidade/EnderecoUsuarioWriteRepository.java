package repasse.phcauto.backend.infra.database.repository.write.identidade;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.infra.database.entity.identidade.EnderecoUsuarioEntity;

public interface EnderecoUsuarioWriteRepository extends JpaRepository<EnderecoUsuarioEntity, UUID> {
}
