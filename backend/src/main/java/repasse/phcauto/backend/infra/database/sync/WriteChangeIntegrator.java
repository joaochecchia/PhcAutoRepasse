package repasse.phcauto.backend.infra.database.sync;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.integrator.spi.Integrator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Instalado exclusivamente na unidade de escrita. Captura também dirty checking. */
public class WriteChangeIntegrator implements Integrator {
    private static final Object PUBLISHED_ROWS_RESOURCE = new Object();
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

    void publish(Class<?> type, Object id) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Alterações exigem uma transação Spring no banco write");
        }
        var table = ProjectionTable.forEntity(type);
        var key = new ChangedRow(table, table.key(id));
        if (!publishedRows().add(key)) return;
        events.publishEvent(RowChanged.of(table, key.key()));
    }

    @SuppressWarnings("unchecked")
    private Set<ChangedRow> publishedRows() {
        var existing = TransactionSynchronizationManager.getResource(PUBLISHED_ROWS_RESOURCE);
        if (existing != null) return (Set<ChangedRow>) existing;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Sincronização transacional obrigatória no banco write");
        }
        Set<ChangedRow> rows = new HashSet<>();
        TransactionSynchronizationManager.bindResource(PUBLISHED_ROWS_RESOURCE, rows);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(PUBLISHED_ROWS_RESOURCE);
            }
        });
        return rows;
    }

    private record ChangedRow(ProjectionTable table, List<String> key) { }
}
