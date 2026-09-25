package repasse.phcauto.backend.usuarios;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class UsuariosArchitectureTests {
    @Test void coreNaoDependeDeFrameworksNemInfraestrutura() {
        var classes = new ClassFileImporter().importPackages("repasse.phcauto.backend.usuarios");
        noClasses().that().resideInAPackage("..usuarios.internal.core..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..",
                        "org.hibernate..", "..infrastructure..", "..infra..").check(classes);
    }
}
