package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.DadosCompraPjEntity;

public interface DadosCompraPjWriteRepository extends JpaRepository<DadosCompraPjEntity, UUID> {
}
