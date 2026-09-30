package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.read;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.DadosCompraPjEntity;

public interface DadosCompraPjReadRepository extends ReadOnlyRepository<DadosCompraPjEntity, UUID> {
}
