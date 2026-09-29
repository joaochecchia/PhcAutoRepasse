package repasse.phcauto.backend.compliance.internal.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.compliance.internal.core.AceiteTermos;
import repasse.phcauto.backend.compliance.internal.core.AceiteTermosGateway;
import repasse.phcauto.backend.compliance.internal.infrastructure.entity.AceiteTermosEntity;
import repasse.phcauto.backend.compliance.internal.infrastructure.repository.AceiteTermosWriteRepository;

@Component
class JpaAceiteTermosGateway implements AceiteTermosGateway {
    private final AceiteTermosWriteRepository repository;

    JpaAceiteTermosGateway(AceiteTermosWriteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
    public void salvar(AceiteTermos aceite) {
        repository.save(AceiteTermosEntity.criar(aceite));
    }
}
