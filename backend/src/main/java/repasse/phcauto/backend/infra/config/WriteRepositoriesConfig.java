package repasse.phcauto.backend.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        repositoryBaseClass = repasse.phcauto.backend.infra.database.sync.EventAwareJpaRepository.class,
        basePackages = {"repasse.phcauto.backend.infra.database.repository.write",
                "repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write"},
        entityManagerFactoryRef = "writeEntityManagerFactory",
        transactionManagerRef = "writeTransactionManager")
public class WriteRepositoriesConfig { }
