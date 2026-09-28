package repasse.phcauto.backend.anuncios.internal.infrastructure;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.anuncios.AnuncioCriado;
import repasse.phcauto.backend.anuncios.internal.core.PublicarAlteracaoAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.PublicarAnuncioCriadoGateway;

@Component
@Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
class SpringAnuncioCriadoPublisher implements PublicarAnuncioCriadoGateway, PublicarAlteracaoAnuncioGateway {
    private final ApplicationEventPublisher events;
    SpringAnuncioCriadoPublisher(ApplicationEventPublisher events) { this.events = events; }
    @Override public void publicar(AnuncioCriado evento) { events.publishEvent(evento); }
    @Override public void publicar(Object evento) { events.publishEvent(evento); }
}
