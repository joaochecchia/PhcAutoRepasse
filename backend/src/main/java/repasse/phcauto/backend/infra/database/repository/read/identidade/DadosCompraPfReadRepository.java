package repasse.phcauto.backend.infra.database.repository.read.identidade;

import java.util.UUID;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import repasse.phcauto.backend.infra.database.entity.identidade.DadosCompraPfEntity;

public interface DadosCompraPfReadRepository extends ReadOnlyRepository<DadosCompraPfEntity, UUID> {
}
