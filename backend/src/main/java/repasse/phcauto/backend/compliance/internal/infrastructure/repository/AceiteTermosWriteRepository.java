package repasse.phcauto.backend.compliance.internal.infrastructure.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.compliance.internal.infrastructure.entity.AceiteTermosEntity;

public interface AceiteTermosWriteRepository extends JpaRepository<AceiteTermosEntity, UUID> {
}
