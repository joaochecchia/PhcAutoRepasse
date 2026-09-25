package repasse.phcauto.backend.infra.database.repository.read.identidade;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.infra.database.entity.identidade.IdentidadeExternaEntity;
import java.util.Optional;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

public interface IdentidadeExternaReadRepository extends ReadOnlyRepository<IdentidadeExternaEntity, UUID> {
    Optional<IdentidadeExternaEntity> findByProvedorAndIdentificadorExterno(ProvedorAutenticacao provedor, String identificadorExterno);
}
