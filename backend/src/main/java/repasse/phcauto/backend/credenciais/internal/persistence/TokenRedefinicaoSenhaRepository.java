package repasse.phcauto.backend.credenciais.internal.persistence;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenhaEntity,UUID>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TokenRedefinicaoSenhaEntity t where t.tokenHash=:hash")
    Optional<TokenRedefinicaoSenhaEntity> buscarParaAtualizar(@Param("hash") String hash);
    @Modifying @Query("update TokenRedefinicaoSenhaEntity t set t.revogadoEm=:agora where t.usuarioId=:usuario and t.usadoEm is null and t.revogadoEm is null")
    int revogarDoUsuario(@Param("usuario") UUID usuario,@Param("agora") Instant agora);
}
