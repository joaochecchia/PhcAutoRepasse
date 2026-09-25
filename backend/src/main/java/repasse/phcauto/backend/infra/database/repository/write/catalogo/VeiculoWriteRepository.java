package repasse.phcauto.backend.infra.database.repository.write.catalogo;

import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VeiculoWriteRepository extends JpaRepository<VeiculoEntity, UUID> {
}
