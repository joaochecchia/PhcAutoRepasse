package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.AnuncioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AnuncioWriteRepository extends JpaRepository<AnuncioEntity, UUID> {
}
