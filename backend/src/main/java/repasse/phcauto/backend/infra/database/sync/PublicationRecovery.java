package repasse.phcauto.backend.infra.database.sync;

import java.time.Duration;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.usuarios.UsuarioCriado;

@Component
public class PublicationRecovery {
    private final FailedEventPublications publications;
    public PublicationRecovery(FailedEventPublications publications) { this.publications = publications; }

    @Scheduled(fixedDelayString = "${app.projection.retry-delay:30s}")
    public void retryFailed() {
        publications.resubmit(ResubmissionOptions.defaults().withBatchSize(100).withMaxInFlight(100)
                .withMinAge(Duration.ofSeconds(1)).withFilter(p -> supports(p.getEvent())));
    }
    static boolean supports(Object event) {
        return event instanceof RowChanged || event instanceof UsuarioAlterado || event instanceof UsuarioCriado;
    }
}
