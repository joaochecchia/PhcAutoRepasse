package repasse.phcauto.backend.anuncios.internal.infrastructure.repository.write;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.anuncios.internal.infrastructure.entity.EnderecoAnuncioEntity;

public interface EnderecoAnuncioWriteRepository extends JpaRepository<EnderecoAnuncioEntity, UUID> {
}
