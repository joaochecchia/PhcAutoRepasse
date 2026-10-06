package repasse.phcauto.backend.infra.database.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity;

class WriteChangeIntegratorTests {
    @AfterEach void cleanup() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.getSynchronizations().forEach(
                    synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test void publicaUmaVezPorLinhaDentroDaMesmaTransacao() {
        var published = new ArrayList<Object>();
        ApplicationEventPublisher events = published::add;
        var integrator = new WriteChangeIntegrator(events);
        var id = UUID.randomUUID();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        integrator.publish(UsuarioEntity.class, id);
        integrator.publish(UsuarioEntity.class, id);

        assertEquals(1, published.size());
    }

    @Test void permiteNovoEventoDaMesmaLinhaEmOutraTransacao() {
        var published = new ArrayList<Object>();
        ApplicationEventPublisher events = published::add;
        var integrator = new WriteChangeIntegrator(events);
        var id = UUID.randomUUID();
        for (int transaction = 0; transaction < 2; transaction++) {
            TransactionSynchronizationManager.setActualTransactionActive(true);
            TransactionSynchronizationManager.initSynchronization();
            integrator.publish(UsuarioEntity.class, id);
            TransactionSynchronizationManager.getSynchronizations().forEach(
                    synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }

        assertEquals(2, published.size());
    }
}
