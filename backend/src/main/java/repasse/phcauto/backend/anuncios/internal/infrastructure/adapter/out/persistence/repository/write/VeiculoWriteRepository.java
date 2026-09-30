package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VeiculoWriteRepository extends JpaRepository<VeiculoEntity, UUID> {
}
