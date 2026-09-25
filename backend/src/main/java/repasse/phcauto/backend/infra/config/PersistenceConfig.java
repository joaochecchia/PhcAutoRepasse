package repasse.phcauto.backend.infra.config;

import java.util.Map;
import java.util.List;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.support.JdbcTransactionManager;
import repasse.phcauto.backend.infra.database.sync.WriteChangeIntegrator;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;

@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
public class PersistenceConfig {

    @Bean(name = {"writeDataSource", "dataSource"})
    @Primary
    @ConfigurationProperties("app.datasource.write")
    public HikariDataSource writeDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @ConfigurationProperties("app.datasource.read")
    public HikariDataSource readDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @ConfigurationProperties("app.datasource.projection")
    public HikariDataSource projectionDataSource() { return new HikariDataSource(); }

    @Bean(initMethod = "migrate")
    public Flyway projectionFlyway(@Qualifier("projectionDataSource") DataSource source) {
        return Flyway.configure().dataSource(source)
                .locations("classpath:db/migration", "classpath:db/read").load();
    }

    @Bean
    public PlatformTransactionManager projectionTransactionManager(
            @Qualifier("projectionDataSource") DataSource source) {
        return new JdbcTransactionManager(source);
    }

    // Registros Modulith existem somente na origem; ambas as bases recebem suas migrations.
    @Bean(initMethod = "migrate")
    public Flyway primaryFlyway(@Qualifier("writeDataSource") DataSource source) {
        return Flyway.configure().dataSource(source)
                .locations("classpath:db/migration", "classpath:db/write").load();
    }

    @Bean(name = {"writeEntityManagerFactory", "entityManagerFactory"})
    @Primary
    @DependsOn("primaryFlyway")
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
            @Qualifier("writeDataSource") DataSource source, ApplicationEventPublisher events) {
        var factory = factory(source, "write");
        factory.getJpaPropertyMap().put("hibernate.integrator_provider",
                (IntegratorProvider) () -> List.of(new WriteChangeIntegrator(events)));
        return factory;
    }

    @Bean
    @DependsOn("projectionFlyway")
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
            @Qualifier("readDataSource") DataSource source) {
        return factory(source, "read");
    }

    @Bean(name = {"writeTransactionManager", "transactionManager"})
    @Primary
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Bean
    public PlatformTransactionManager readTransactionManager(
            @Qualifier("readEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    private LocalContainerEntityManagerFactoryBean factory(DataSource source, String unit) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source);
        factory.setPersistenceUnitName(unit);
        factory.setPackagesToScan("repasse.phcauto.backend.infra.database.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "validate",
                "hibernate.jdbc.time_zone", "UTC"));
        return factory;
    }

}
