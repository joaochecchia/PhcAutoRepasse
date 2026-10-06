package repasse.phcauto.backend.usuarios.internal.core.domain;

public record EnderecoUsuarioDados(String cep, String cidade, String bairro,
        String rua, String numero, String complemento, String uf, Integer municipioCodigoIbge) {
    public EnderecoUsuarioDados(String cep, String cidade, String bairro, String rua,
            String numero, String complemento, String uf) {
        this(cep, cidade, bairro, rua, numero, complemento, uf, null);
    }
}
