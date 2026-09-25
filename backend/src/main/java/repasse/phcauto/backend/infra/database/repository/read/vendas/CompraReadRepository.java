package repasse.phcauto.backend.infra.database.repository.read.vendas;

import repasse.phcauto.backend.infra.database.entity.vendas.CompraEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface CompraReadRepository extends ReadOnlyRepository<CompraEntity, UUID> {
}
