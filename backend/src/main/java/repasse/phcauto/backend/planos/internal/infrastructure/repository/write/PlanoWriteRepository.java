package repasse.phcauto.backend.planos.internal.infrastructure.repository.write;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.planos.internal.infrastructure.entity.PlanoEntity;

public interface PlanoWriteRepository extends JpaRepository<PlanoEntity, UUID> {
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, UUID id);
}
