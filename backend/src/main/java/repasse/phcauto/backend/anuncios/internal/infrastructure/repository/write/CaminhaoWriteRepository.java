package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.CaminhaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CaminhaoWriteRepository extends JpaRepository<CaminhaoEntity, UUID> {
}
