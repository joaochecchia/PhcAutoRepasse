package repasse.phcauto.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTests {

    @Test
    void verificaModulosHttpEUsuarios() {
        var modules = ApplicationModules.of(BackendApplication.class);
        modules.verify();
        var nomes = StreamSupport.stream(modules.spliterator(), false)
                .map(module -> module.getIdentifier().toString())
                .collect(Collectors.toSet());
        assertEquals(Set.of("identidade", "assinaturas", "catalogo", "vendas", "usuarios"), nomes);
    }
}
