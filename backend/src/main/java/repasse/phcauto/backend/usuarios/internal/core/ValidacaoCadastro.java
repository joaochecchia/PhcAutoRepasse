package repasse.phcauto.backend.usuarios.internal.core;

final class ValidacaoCadastro {
    private ValidacaoCadastro() { }
    static String texto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank() || valor.strip().length() > max) {
            throw new CadastroInvalidoException(campo + " obrigatório ou acima do tamanho permitido");
        }
        return valor.strip();
    }
    static String opcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) return null;
        return texto(valor, campo, max);
    }
    static boolean documentoValido(String valor, boolean cpf) {
        int tamanho = cpf ? 11 : 14;
        if (valor == null || !valor.matches("[0-9]{" + tamanho + "}")
                || valor.chars().distinct().count() == 1) return false;
        for (int fim = tamanho - 2; fim < tamanho; fim++) {
            int soma = 0;
            for (int i = 0; i < fim; i++) {
                int peso = cpf ? fim + 1 - i : (fim - 1 - i) % 8 + 2;
                soma += (valor.charAt(i) - '0') * peso;
            }
            int resto = soma % 11;
            int digito = resto < 2 ? 0 : 11 - resto;
            if (digito != valor.charAt(fim) - '0') return false;
        }
        return true;
    }
}
