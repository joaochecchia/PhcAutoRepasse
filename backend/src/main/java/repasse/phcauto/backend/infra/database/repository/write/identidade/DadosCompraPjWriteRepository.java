package repasse.phcauto.backend.infra.database.repository.write.identidade;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.infra.database.entity.identidade.DadosCompraPjEntity;

public interface DadosCompraPjWriteRepository extends JpaRepository<DadosCompraPjEntity, UUID> {
}
