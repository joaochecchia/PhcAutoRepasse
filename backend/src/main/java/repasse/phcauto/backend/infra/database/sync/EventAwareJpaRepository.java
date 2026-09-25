package repasse.phcauto.backend.infra.database.sync;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

/** Exclusões em lote preservam callbacks JPA e os eventos de sincronização. */
public class EventAwareJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> {
    public EventAwareJpaRepository(JpaEntityInformation<T, ?> information, EntityManager manager) {
        super(information, manager);
    }
    @Override
    @Transactional("writeTransactionManager")
    public void deleteAllInBatch() { super.deleteAll(); }
    @Override
    @Transactional("writeTransactionManager")
    public void deleteAllInBatch(Iterable<T> entities) { super.deleteAll(entities); }
    @Override
    @Transactional("writeTransactionManager")
    public void deleteAllByIdInBatch(Iterable<ID> ids) { super.deleteAllById(ids); }
}
