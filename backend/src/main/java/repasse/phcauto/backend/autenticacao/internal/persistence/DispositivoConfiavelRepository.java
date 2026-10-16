package repasse.phcauto.backend.autenticacao.internal.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoConfiavelRepository extends JpaRepository<DispositivoConfiavelEntity, UUID> {
    Optional<DispositivoConfiavelEntity> findByTokenHash(String tokenHash);
    List<DispositivoConfiavelEntity> findByUsuarioIdOrderByUltimoUsoEmDesc(UUID usuarioId);
}
