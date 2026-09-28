package repasse.phcauto.backend.infra.database.repository.write.catalogo;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.infra.database.entity.catalogo.EnderecoAnuncioEntity;

public interface EnderecoAnuncioWriteRepository extends JpaRepository<EnderecoAnuncioEntity, UUID> {
}
