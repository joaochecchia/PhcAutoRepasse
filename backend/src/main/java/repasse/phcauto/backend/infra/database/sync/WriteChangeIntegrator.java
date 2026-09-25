package repasse.phcauto.backend.infra.database.sync;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.integrator.spi.Integrator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Instalado exclusivamente na unidade de escrita. Captura também dirty checking. */
public class WriteChangeIntegrator implements Integrator {
    private final ApplicationEventPublisher events;

    public WriteChangeIntegrator(ApplicationEventPublisher events) { this.events = events; }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrap, SessionFactoryImplementor factory) {
        var registry = factory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, (PostInsertEventListener) event ->
                publish(event.getPersister().getMappedClass(), event.getId()));
        registry.appendListeners(EventType.POST_UPDATE, (PostUpdateEventListener) event ->
                publish(event.getPersister().getMappedClass(), event.getId()));
        registry.appendListeners(EventType.POST_DELETE, (PostDeleteEventListener) event ->
                publish(event.getPersister().getMappedClass(), event.getId()));
    }

    private void publish(Class<?> type, Object id) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Alterações exigem uma transação Spring no banco write");
        }
        var table = ProjectionTable.forEntity(type);
        events.publishEvent(RowChanged.of(table, table.key(id)));
    }
}
