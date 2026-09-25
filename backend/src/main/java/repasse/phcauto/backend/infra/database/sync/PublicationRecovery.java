package repasse.phcauto.backend.infra.database.sync;

import java.time.Duration;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PublicationRecovery {
    private final FailedEventPublications publications;
    public PublicationRecovery(FailedEventPublications publications) { this.publications = publications; }

    @Scheduled(fixedDelayString = "${app.projection.retry-delay:30s}")
    public void retryFailed() {
        publications.resubmit(ResubmissionOptions.defaults().withBatchSize(100).withMaxInFlight(100)
                .withMinAge(Duration.ofSeconds(1)).withFilter(p -> p.getEvent() instanceof RowChanged));
    }
}
