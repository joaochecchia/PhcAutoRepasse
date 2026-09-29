package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.FotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FotoWriteRepository extends JpaRepository<FotoEntity, UUID> {
    java.util.List<FotoEntity> findByAnuncioId(UUID anuncioId);
}
