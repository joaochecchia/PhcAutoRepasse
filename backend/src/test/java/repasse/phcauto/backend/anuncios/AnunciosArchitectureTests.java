package repasse.phcauto.backend.anuncios;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class AnunciosArchitectureTests {
    @Test
    void coreNaoDependeDeFrameworksNemInfraestrutura() {
        var classes = new ClassFileImporter().importPackages("repasse.phcauto.backend.anuncios");
        noClasses().that().resideInAPackage("..anuncios.internal.core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta..", "org.hibernate..",
                        "..infrastructure..", "..infra..")
                .check(classes);
    }
}
