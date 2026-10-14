package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.core.gateway.RevogarSessoesGateway;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.RefreshTokenSessionWriteRepository;

@Component
@Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
public class JpaRevogarSessoesGateway implements RevogarSessoesGateway {
    private final RefreshTokenSessionWriteRepository sessoes;

    public JpaRevogarSessoesGateway(RefreshTokenSessionWriteRepository sessoes) {
        this.sessoes = sessoes;
    }

    @Override
    public void revogarTodas(UUID usuarioId, Instant agora) {
        sessoes.revogarTodasDoUsuario(usuarioId, agora);
    }
}
