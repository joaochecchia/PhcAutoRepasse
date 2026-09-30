package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioAdminEntity;
public interface UsuarioAdminWriteRepository extends JpaRepository<UsuarioAdminEntity, UUID> {
    boolean existsByCpf(String cpf);
    boolean existsByCnpj(String cnpj);
}
