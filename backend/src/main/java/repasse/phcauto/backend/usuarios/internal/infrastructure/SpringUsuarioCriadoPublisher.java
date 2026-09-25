package repasse.phcauto.backend.usuarios.internal.infrastructure;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import repasse.phcauto.backend.usuarios.UsuarioCriado;
import repasse.phcauto.backend.usuarios.internal.core.PublicarUsuarioCriadoGateway;

@Component
public class SpringUsuarioCriadoPublisher implements PublicarUsuarioCriadoGateway {
    private final ApplicationEventPublisher publisher;
    public SpringUsuarioCriadoPublisher(ApplicationEventPublisher publisher) { this.publisher = publisher; }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
    public void publicar(UsuarioCriado evento) {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("Publicação exige transação de escrita");
        }
        publisher.publishEvent(evento);
    }
}
