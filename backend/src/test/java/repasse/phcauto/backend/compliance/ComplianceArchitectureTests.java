package repasse.phcauto.backend.compliance;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ComplianceArchitectureTests {
    @Test
    void coreNaoDependeDeFrameworksNemInfraestrutura() {
        var classes = new ClassFileImporter().importPackages("repasse.phcauto.backend.compliance");
        noClasses().that().resideInAPackage("..compliance.internal.core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta..", "org.hibernate..",
                        "..infrastructure..", "..infra..")
                .check(classes);
    }

    @Test
    void fachadaPublicaNaoDependeDeSpring() {
        assertTrue(ComplianceFacade.class.isInterface());
        assertFalse(ComplianceFacade.class.isAnnotationPresent(
                org.springframework.stereotype.Service.class));
        assertFalse(ComplianceFacade.class.isAnnotationPresent(
                org.springframework.transaction.annotation.Transactional.class));
    }
}
