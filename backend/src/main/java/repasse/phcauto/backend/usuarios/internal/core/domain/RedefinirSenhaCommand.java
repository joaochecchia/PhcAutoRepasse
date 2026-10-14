package repasse.phcauto.backend.usuarios.internal.core.domain;

import repasse.phcauto.backend.usuarios.internal.core.validation.PoliticaSenha;

public record RedefinirSenhaCommand(String novaSenha) {
    public RedefinirSenhaCommand {
        PoliticaSenha.validar(novaSenha);
    }

    @Override public String toString() { return "RedefinirSenhaCommand[conteudo protegido]"; }
}
