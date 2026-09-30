package repasse.phcauto.backend.planos;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import repasse.phcauto.backend.planos.internal.infrastructure.service.PlanosFacade;
class PlanosArchitectureTests {
    @Test void coreNaoDependeDeFrameworksNemInfraestrutura() {
        var classes = new ClassFileImporter().importPackages("repasse.phcauto.backend.planos");
        noClasses().that().resideInAPackage("..planos.internal.core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta..", "org.hibernate..",
                        "..infrastructure..", "..infra..")
                .check(classes);
    }
    @Test void fachadaDeAplicacaoPermaneceSemSpring() {
        assertTrue(PlanosFacade.class.isInterface());
        assertFalse(PlanosFacade.class.isAnnotationPresent(org.springframework.stereotype.Service.class));
        assertFalse(PlanosFacade.class.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
    }
}
