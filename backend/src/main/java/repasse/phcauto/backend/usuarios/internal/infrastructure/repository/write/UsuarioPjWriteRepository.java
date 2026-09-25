package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioPjEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioPjWriteRepository extends JpaRepository<UsuarioPjEntity, UUID> {
    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndUsuarioIdNot(String cnpj, UUID usuarioId);
}
