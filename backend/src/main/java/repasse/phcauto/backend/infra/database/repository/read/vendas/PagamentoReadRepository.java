package repasse.phcauto.backend.infra.database.repository.read.vendas;

import repasse.phcauto.backend.infra.database.entity.vendas.PagamentoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface PagamentoReadRepository extends ReadOnlyRepository<PagamentoEntity, UUID> {
}
