package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.read;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioAdminEntity;
public interface UsuarioAdminReadRepository extends Repository<UsuarioAdminEntity, UUID> {
    java.util.Optional<UsuarioAdminEntity> findById(UUID id);
}
