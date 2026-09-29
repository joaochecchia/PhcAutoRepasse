package repasse.phcauto.backend.compliance.internal.infrastructure;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repasse.phcauto.backend.compliance.internal.core.AceiteTermosGateway;
import repasse.phcauto.backend.compliance.internal.core.RegistrarAceite;
import repasse.phcauto.backend.compliance.internal.core.RegistrarAceiteUseCase;
import repasse.phcauto.backend.compliance.internal.core.VersoesDocumentosVigentes;

@Configuration(proxyBeanMethods = false)
class ComplianceConfiguration {
    @Bean
    VersoesDocumentosVigentes versoesDocumentosVigentes(
            @Value("${app.compliance.termos-uso-versao}") String termos,
            @Value("${app.compliance.politica-privacidade-versao}") String privacidade) {
        return new VersoesDocumentosVigentes(termos, privacidade);
    }

    @Bean
    RegistrarAceiteUseCase registrarAceiteUseCase(AceiteTermosGateway gateway,
            VersoesDocumentosVigentes versoes, Clock applicationClock) {
        return new RegistrarAceite(gateway, versoes, applicationClock);
    }
}
