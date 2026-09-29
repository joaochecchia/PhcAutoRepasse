package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.AnuncioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AnuncioWriteRepository extends JpaRepository<AnuncioEntity, UUID> {
}
