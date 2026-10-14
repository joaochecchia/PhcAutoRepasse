package repasse.phcauto.backend.infra.observability;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProjectionObservability {
    private static final Logger log = LoggerFactory.getLogger(ProjectionObservability.class);
    private final JdbcTemplate jdbc;
    private final MeterRegistry metrics;
    private final AtomicLong pendentes = new AtomicLong();
    private final AtomicLong idadeMaisAntigaSegundos = new AtomicLong();

    public ProjectionObservability(@Qualifier("writeDataSource") DataSource dataSource, MeterRegistry metrics) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.metrics = metrics;
        metrics.gauge("phcauto.projection.pending", pendentes);
        metrics.gauge("phcauto.projection.oldest.seconds", idadeMaisAntigaSegundos);
    }

    @Scheduled(fixedDelayString = "${app.observability.projection-check-interval:30s}")
    public void atualizar() {
        try {
            var estado = jdbc.queryForObject("""
                    select count(*) as quantidade,
                           coalesce(extract(epoch from (current_timestamp - min(publication_date))), 0)::bigint as idade
                      from event_publication
                     where completion_date is null
                    """, (rs, row) -> new Estado(rs.getLong("quantidade"), rs.getLong("idade")));
            pendentes.set(estado == null ? 0 : estado.quantidade());
            idadeMaisAntigaSegundos.set(estado == null ? 0 : Math.max(0, estado.idadeSegundos()));
        } catch (RuntimeException error) {
            metrics.counter("phcauto.projection.observation.errors").increment();
            log.error("Falha ao medir publicações pendentes da projeção read", error);
        }
    }

    private record Estado(long quantidade, long idadeSegundos) { }
}
