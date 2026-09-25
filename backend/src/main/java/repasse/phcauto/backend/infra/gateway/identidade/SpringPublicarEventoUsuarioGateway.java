package repasse.phcauto.backend.infra.gateway.identidade;

import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.domain.gateway.identidade.PublicarEventoUsuarioGateway;

@Component
public class SpringPublicarEventoUsuarioGateway implements PublicarEventoUsuarioGateway {
    private final ApplicationEventPublisher publisher;

    public SpringPublicarEventoUsuarioGateway(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
    public void publicar(UsuarioAlterado evento) {
        Objects.requireNonNull(evento, "evento");
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("Eventos de usuário exigem uma transação de escrita");
        }
        publisher.publishEvent(evento);
    }
}
