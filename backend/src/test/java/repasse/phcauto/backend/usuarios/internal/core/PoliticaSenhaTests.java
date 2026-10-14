package repasse.phcauto.backend.usuarios.internal.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.core.validation.PoliticaSenha;

class PoliticaSenhaTests {
    @Test void aceitaSenhaComTodosOsCriterios() {
        assertDoesNotThrow(() -> PoliticaSenha.validar("Senha@123"));
    }

    @Test void rejeitaCadaCriterioAusente() {
        for (String senha : new String[] {"Aa1!", "senha@123", "SENHA@123", "Senha@Teste", "Senha1234"}) {
            assertThrows(CadastroInvalidoException.class, () -> PoliticaSenha.validar(senha), senha);
        }
    }
}
