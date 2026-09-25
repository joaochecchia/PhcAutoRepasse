package repasse.phcauto.backend.infra.database.repository.write.identidade;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.infra.database.entity.identidade.IdentidadeExternaEntity;
import java.util.Optional;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

public interface IdentidadeExternaWriteRepository extends JpaRepository<IdentidadeExternaEntity, UUID> {
    Optional<IdentidadeExternaEntity> findByProvedorAndIdentificadorExterno(ProvedorAutenticacao provedor, String identificadorExterno);
}
