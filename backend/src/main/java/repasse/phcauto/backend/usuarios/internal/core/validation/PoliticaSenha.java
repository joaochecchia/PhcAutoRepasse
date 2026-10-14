package repasse.phcauto.backend.usuarios.internal.core.validation;

import java.nio.charset.StandardCharsets;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;

/** Política de senha do domínio, independente de HTTP e Spring. */
public final class PoliticaSenha {
    private PoliticaSenha() { }

    public static void validar(String senha) {
        if (senha == null || senha.length() < 8) {
            throw invalida();
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72
                || senha.codePoints().noneMatch(Character::isUpperCase)
                || senha.codePoints().noneMatch(Character::isLowerCase)
                || senha.codePoints().noneMatch(Character::isDigit)
                || senha.codePoints().noneMatch(PoliticaSenha::especial)) {
            throw invalida();
        }
    }

    private static boolean especial(int caractere) {
        return !Character.isLetterOrDigit(caractere) && !Character.isWhitespace(caractere);
    }

    private static CadastroInvalidoException invalida() {
        return new CadastroInvalidoException(
                "Senha deve ter no mínimo 8 caracteres, com letra maiúscula, letra minúscula, número e caractere especial");
    }
}
