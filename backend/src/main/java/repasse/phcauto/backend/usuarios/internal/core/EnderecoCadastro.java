package repasse.phcauto.backend.usuarios.internal.core;

public record EnderecoCadastro(String cep, String cidade, String bairro, String rua,
        String numero, String complemento, String uf) {
    public EnderecoCadastro {
        cep = ValidacaoCadastro.texto(cep, "CEP", 8);
        if (!cep.matches("[0-9]{8}")) throw new CadastroInvalidoException("CEP inválido");
        cidade = ValidacaoCadastro.texto(cidade, "Cidade", 120);
        bairro = ValidacaoCadastro.texto(bairro, "Bairro", 120);
        rua = ValidacaoCadastro.texto(rua, "Rua", 200);
        numero = ValidacaoCadastro.opcional(numero, "Número", 20);
        complemento = ValidacaoCadastro.opcional(complemento, "Complemento", 200);
        uf = ValidacaoCadastro.texto(uf, "UF", 2).toUpperCase(java.util.Locale.ROOT);
        if (!java.util.Set.of("AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT",
                "MS","MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO").contains(uf)) {
            throw new CadastroInvalidoException("UF inválida");
        }
    }
}
