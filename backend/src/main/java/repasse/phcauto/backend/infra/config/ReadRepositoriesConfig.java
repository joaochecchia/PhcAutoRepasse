package repasse.phcauto.backend.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackages = {"repasse.phcauto.backend.infra.database.repository.read",
                "repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.read",
                "repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.read",
                "repasse.phcauto.backend.planos.internal.infrastructure.adapter.out.persistence.repository.read"},
        entityManagerFactoryRef = "readEntityManagerFactory",
        transactionManagerRef = "readTransactionManager")
public class ReadRepositoriesConfig { }
