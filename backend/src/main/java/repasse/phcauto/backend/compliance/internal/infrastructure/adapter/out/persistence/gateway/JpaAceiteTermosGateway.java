package repasse.phcauto.backend.compliance.internal.infrastructure.adapter.out.persistence.gateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.compliance.internal.core.domain.AceiteTermos;
import repasse.phcauto.backend.compliance.internal.core.gateway.AceiteTermosGateway;
import repasse.phcauto.backend.compliance.internal.infrastructure.adapter.out.persistence.entity.AceiteTermosEntity;
import repasse.phcauto.backend.compliance.internal.infrastructure.adapter.out.persistence.repository.AceiteTermosWriteRepository;

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
