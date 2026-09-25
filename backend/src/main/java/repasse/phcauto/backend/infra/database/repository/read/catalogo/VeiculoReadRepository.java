package repasse.phcauto.backend.infra.database.repository.read.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoEntity;
import repasse.phcauto.backend.infra.database.repository.read.ReadOnlyRepository;
import java.util.UUID;

public interface VeiculoReadRepository extends ReadOnlyRepository<VeiculoEntity, UUID> {
}
