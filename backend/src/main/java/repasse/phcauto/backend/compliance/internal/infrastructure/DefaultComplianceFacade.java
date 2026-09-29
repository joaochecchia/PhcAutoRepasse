package repasse.phcauto.backend.compliance.internal.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.compliance.ComplianceFacade;
import repasse.phcauto.backend.compliance.RegistrarAceiteCadastroRequest;
import repasse.phcauto.backend.compliance.internal.core.RegistrarAceiteUseCase;

@Service
class DefaultComplianceFacade implements ComplianceFacade {
    private final RegistrarAceiteUseCase registrarAceite;

    DefaultComplianceFacade(RegistrarAceiteUseCase registrarAceite) {
        this.registrarAceite = registrarAceite;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
    public void registrarAceiteCadastro(RegistrarAceiteCadastroRequest request) {
        registrarAceite.execute(request);
    }
}
