package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;
import java.time.Clock;
import java.util.UUID;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;
import repasse.phcauto.backend.usuarios.internal.core.gateway.SalvarFotoPerfilGateway;
import repasse.phcauto.backend.usuarios.internal.core.exception.UsuarioNaoEncontradoException;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
@Repository
@Transactional(transactionManager="writeTransactionManager", propagation=Propagation.MANDATORY)
public class JpaSalvarFotoPerfilGateway implements SalvarFotoPerfilGateway {
    @PersistenceContext(unitName="write") private EntityManager manager;
    private final Clock clock;
    public JpaSalvarFotoPerfilGateway(Clock applicationClock) { this.clock = applicationClock; }
    public String salvar(UUID id, String chave) {
        var usuario = manager.find(UsuarioEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (usuario == null || !usuario.getAtivo()) throw new UsuarioNaoEncontradoException();
        String anterior = usuario.getFotoPerfilChave();
        usuario.alterarFotoPerfil(chave, clock.instant());
        manager.flush();
        return anterior;
    }
}
