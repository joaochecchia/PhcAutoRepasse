package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioWriteRepository extends JpaRepository<UsuarioEntity, UUID> {
    java.util.List<UsuarioEntity> findTop2ByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
