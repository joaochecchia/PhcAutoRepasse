package repasse.phcauto.backend.usuarios.internal.infrastructure;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.usuarios.internal.core.PublicarUsuarioAlteradoGateway;

@Component
public class SpringUsuarioAlteradoPublisher implements PublicarUsuarioAlteradoGateway {
    private final ApplicationEventPublisher publisher;

    public SpringUsuarioAlteradoPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
    public void publicar(UsuarioAlterado evento) {
        publisher.publishEvent(evento);
    }
}
