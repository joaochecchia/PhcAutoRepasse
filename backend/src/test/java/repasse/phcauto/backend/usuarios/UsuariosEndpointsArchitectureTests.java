package repasse.phcauto.backend.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.controller.UsuariosController;

class UsuariosEndpointsArchitectureTests {
    @Test void somenteControllerDoAgregadoExpoePfPjEEndereco() {
        for (String removido : new String[] {
                "repasse.phcauto.backend.infra.controller.identidade.UsuarioController",
                "repasse.phcauto.backend.infra.controller.identidade.UsuarioPfController",
                "repasse.phcauto.backend.infra.controller.identidade.UsuarioPjController",
                "repasse.phcauto.backend.infra.controller.identidade.EnderecoUsuarioController"}) {
            assertThrows(ClassNotFoundException.class, () -> Class.forName(removido));
        }
        Method[] metodos = UsuariosController.class.getDeclaredMethods();
        assertEquals(1, Arrays.stream(metodos).filter(m -> m.isAnnotationPresent(PostMapping.class)).count());
        assertEquals(1, Arrays.stream(metodos).filter(m -> m.isAnnotationPresent(GetMapping.class)).count());
        assertEquals(1, Arrays.stream(metodos).filter(m -> m.isAnnotationPresent(PatchMapping.class)).count());
        assertEquals(1, Arrays.stream(metodos).filter(m -> m.isAnnotationPresent(DeleteMapping.class)).count());
        assertEquals(0, Arrays.stream(metodos).filter(m -> m.isAnnotationPresent(PutMapping.class)).count());
    }
}
