package repasse.phcauto.backend.usuarios.internal.core;
import org.junit.jupiter.api.Test;
import java.util.*;
import repasse.phcauto.backend.usuarios.internal.core.domain.*;
import repasse.phcauto.backend.usuarios.internal.core.usecase.*;
import repasse.phcauto.backend.usuarios.internal.core.exception.*;
import static org.junit.jupiter.api.Assertions.*;
class PerfisPublicosTests {
    @Test void fotoNaoAceitaChaveDeOutroUsuarioOuTraversal() {
        UUID id=UUID.randomUUID();
        var useCase = new SalvarFotoPerfil((usuario,chave) -> { fail("Não deveria persistir referência inválida"); return null; });
        assertThrows(CadastroInvalidoException.class, () -> useCase.execute(id,"usuarios/"+UUID.randomUUID()+"/perfil/"+UUID.randomUUID()+".jpg"));
        assertThrows(CadastroInvalidoException.class, () -> useCase.execute(id,"usuarios/"+id+"/perfil/../../foto.jpg"));
    }
    @Test void fotoAceitaReferenciaPropria() {
        UUID id=UUID.randomUUID(); String chave="usuarios/"+id+"/perfil/"+UUID.randomUUID()+".jpg";
        var useCase = new SalvarFotoPerfil((usuario,arquivo) -> { assertEquals(id,usuario); assertEquals(chave,arquivo); return "anterior"; });
        assertEquals("anterior",useCase.execute(id,chave));
    }
    @Test void paginaTemLimiteECalculoSemOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new FiltroLojistas(null,"Brasília","DF",0,13));
        assertThrows(IllegalArgumentException.class, () -> new FiltroLojistas(null,"Brasília","ZZZ",0,12));
        assertDoesNotThrow(() -> new FiltroLojistas(null,null,null,0,12));
        assertThrows(IllegalArgumentException.class, () -> new FiltroLojistas(null,"Brasília",null,0,12));
        assertThrows(IllegalArgumentException.class, () -> new FiltroLojistas(null,null,null,-15.8,null,0,12));
        assertThrows(IllegalArgumentException.class, () -> new FiltroLojistas(null,null,null,91.0,-47.9,0,12));
        assertThrows(IllegalArgumentException.class, () -> new FiltroLojistas(null,"Brasília","DF",-15.8,-47.9,0,12));
        assertDoesNotThrow(() -> new FiltroLojistas(null,"Brasília","DF",0,12));
        assertDoesNotThrow(() -> new FiltroLojistas(null,null,null,-15.8,-47.9,0,12));
        assertFalse(new PaginaLojistas(List.of(), 1, Integer.MAX_VALUE, 52, 100).temProximaPagina());
        assertTrue(new PaginaLojistas(List.of(), 21, 0, 20, 200).temProximaPagina());
    }
}
