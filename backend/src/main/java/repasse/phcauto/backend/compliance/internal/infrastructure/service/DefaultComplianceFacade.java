package repasse.phcauto.backend.compliance.internal.infrastructure.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.compliance.ComplianceFacade;
import repasse.phcauto.backend.compliance.RegistrarAceiteCadastroCommand;
import repasse.phcauto.backend.compliance.internal.core.usecase.RegistrarAceiteUseCase;

@Service
class DefaultComplianceFacade implements ComplianceFacade {
    private final RegistrarAceiteUseCase registrarAceite;

    DefaultComplianceFacade(RegistrarAceiteUseCase registrarAceite) {
        this.registrarAceite = registrarAceite;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
    public void registrarAceiteCadastro(RegistrarAceiteCadastroCommand request) {
        registrarAceite.execute(request);
    }
}
