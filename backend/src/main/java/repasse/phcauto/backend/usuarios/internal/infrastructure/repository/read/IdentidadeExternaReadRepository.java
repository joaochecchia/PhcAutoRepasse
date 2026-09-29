package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.IdentidadeExternaEntity;
import java.util.Optional;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

public interface IdentidadeExternaReadRepository extends ReadOnlyRepository<IdentidadeExternaEntity, UUID> {
    Optional<IdentidadeExternaEntity> findByProvedorAndIdentificadorExterno(ProvedorAutenticacao provedor, String identificadorExterno);
}
