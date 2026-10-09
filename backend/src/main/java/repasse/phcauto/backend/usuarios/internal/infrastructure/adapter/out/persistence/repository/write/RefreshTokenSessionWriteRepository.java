package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.RefreshTokenSessionEntity;

public interface RefreshTokenSessionWriteRepository extends JpaRepository<RefreshTokenSessionEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RefreshTokenSessionEntity s where s.tokenHash = :hash")
    Optional<RefreshTokenSessionEntity> buscarParaAtualizar(@Param("hash") String hash);

    @Modifying
    @Query("update RefreshTokenSessionEntity s set s.revogadoEm = :agora " +
            "where s.familiaId = :familia and s.revogadoEm is null")
    int revogarFamilia(@Param("familia") UUID familia, @Param("agora") Instant agora);
}
