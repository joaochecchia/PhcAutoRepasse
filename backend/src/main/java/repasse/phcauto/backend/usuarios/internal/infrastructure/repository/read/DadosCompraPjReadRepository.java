package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.DadosCompraPjEntity;

public interface DadosCompraPjReadRepository extends ReadOnlyRepository<DadosCompraPjEntity, UUID> {
}
