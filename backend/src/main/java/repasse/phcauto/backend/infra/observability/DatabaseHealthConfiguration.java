package repasse.phcauto.backend.infra.observability;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DatabaseHealthConfiguration {

    @Bean
    HealthIndicator writeDatabaseHealthIndicator(@Qualifier("writeDataSource") DataSource dataSource) {
        return () -> verificar(dataSource);
    }

    @Bean
    HealthIndicator readDatabaseHealthIndicator(@Qualifier("readDataSource") DataSource dataSource) {
        return () -> verificar(dataSource);
    }

    private static Health verificar(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? Health.up().build() : Health.down().build();
        } catch (Exception error) {
            return Health.down(error).build();
        }
    }
}
