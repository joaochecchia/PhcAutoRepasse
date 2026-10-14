package repasse.phcauto.backend.infra.observability;

import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DependencyAvailabilityMetrics {
    private final DataSource write;
    private final DataSource read;
    private final RedisConnectionFactory redis;
    private final AtomicInteger writeUp = new AtomicInteger();
    private final AtomicInteger readUp = new AtomicInteger();
    private final AtomicInteger redisUp = new AtomicInteger();

    public DependencyAvailabilityMetrics(@Qualifier("writeDataSource") DataSource write,
            @Qualifier("readDataSource") DataSource read, RedisConnectionFactory redis,
            MeterRegistry metrics) {
        this.write = write;
        this.read = read;
        this.redis = redis;
        metrics.gauge("phcauto.dependency.up", java.util.List.of(
                io.micrometer.core.instrument.Tag.of("dependency", "postgres-write")), writeUp);
        metrics.gauge("phcauto.dependency.up", java.util.List.of(
                io.micrometer.core.instrument.Tag.of("dependency", "postgres-read")), readUp);
        metrics.gauge("phcauto.dependency.up", java.util.List.of(
                io.micrometer.core.instrument.Tag.of("dependency", "redis")), redisUp);
    }

    @Scheduled(fixedDelayString = "${app.observability.dependency-check-interval:30s}")
    public void verificar() {
        writeUp.set(disponivel(write));
        readUp.set(disponivel(read));
        redisUp.set(redisDisponivel());
    }

    private static int disponivel(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? 1 : 0;
        } catch (Exception error) {
            return 0;
        }
    }

    private int redisDisponivel() {
        try (var connection = redis.getConnection()) {
            return connection.ping() == null ? 0 : 1;
        } catch (RuntimeException error) {
            return 0;
        }
    }
}
