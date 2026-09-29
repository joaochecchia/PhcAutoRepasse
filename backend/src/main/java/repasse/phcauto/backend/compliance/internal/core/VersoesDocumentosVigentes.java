package repasse.phcauto.backend.compliance.internal.core;

import java.util.Objects;

public record VersoesDocumentosVigentes(
        String termosUso,
        String politicaPrivacidade) {

    public VersoesDocumentosVigentes {
        termosUso = validar(termosUso, "versão vigente dos Termos de Uso");
        politicaPrivacidade = validar(politicaPrivacidade, "versão vigente da Política de Privacidade");
    }

    private static String validar(String valor, String campo) {
        Objects.requireNonNull(valor, campo);
        valor = valor.trim();
        if (valor.isEmpty() || valor.length() > 64) {
            throw new IllegalArgumentException(campo + " inválida");
        }
        return valor;
    }
}
