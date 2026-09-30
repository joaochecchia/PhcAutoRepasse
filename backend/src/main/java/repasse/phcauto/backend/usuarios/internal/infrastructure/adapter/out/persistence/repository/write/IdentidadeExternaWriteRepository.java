package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.IdentidadeExternaEntity;
import java.util.Optional;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

public interface IdentidadeExternaWriteRepository extends JpaRepository<IdentidadeExternaEntity, UUID> {
    Optional<IdentidadeExternaEntity> findByProvedorAndIdentificadorExterno(ProvedorAutenticacao provedor, String identificadorExterno);
}
