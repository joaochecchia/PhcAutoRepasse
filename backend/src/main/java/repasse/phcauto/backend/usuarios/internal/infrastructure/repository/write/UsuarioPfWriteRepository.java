package repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write;

import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.UsuarioPfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioPfWriteRepository extends JpaRepository<UsuarioPfEntity, UUID> {
    boolean existsByCpf(String cpf);

    boolean existsByCpfAndUsuarioIdNot(String cpf, UUID usuarioId);
}
