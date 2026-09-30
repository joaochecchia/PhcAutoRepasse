package repasse.phcauto.backend.planos.internal.core.validation;

import repasse.phcauto.backend.planos.internal.core.exception.PlanoInvalidoException;

public final class PlanoValidator {
    private PlanoValidator() { }

    public static String nome(String nome) {
        if (nome == null || nome.isBlank()) throw new PlanoInvalidoException("Nome do plano é obrigatório");
        var normalizado = nome.trim();
        if (normalizado.length() > 100) throw new PlanoInvalidoException("Nome do plano deve ter no máximo 100 caracteres");
        return normalizado;
    }

    public static void valores(Long valor, Integer periodo, Integer limite, Integer limiteVistorias) {
        if (valor != null && valor < 0) throw new PlanoInvalidoException("Valor do plano não pode ser negativo");
        if (periodo != null && (periodo < 1 || periodo > 32767))
            throw new PlanoInvalidoException("Período deve estar entre 1 e 32767 meses");
        if (limite != null && limite < 0) throw new PlanoInvalidoException("Limite de anúncios não pode ser negativo");
        if (limiteVistorias != null && limiteVistorias < 0)
            throw new PlanoInvalidoException("Limite de vistorias cautelares não pode ser negativo");
    }
}
