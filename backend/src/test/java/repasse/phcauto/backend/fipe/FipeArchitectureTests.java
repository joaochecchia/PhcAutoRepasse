package repasse.phcauto.backend.fipe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
class FipeArchitectureTests {
    @Test void corePuro(){
        noClasses().that().resideInAPackage("..fipe.internal.core..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..","jakarta..","org.hibernate..","..infrastructure..","..infra..")
            .check(new ClassFileImporter().importPackages("repasse.phcauto.backend.fipe"));
    }
}
