package repasse.phcauto.backend.usuarios.response;

public record EnderecoUsuarioResponse(String cep, String cidade, String bairro,
        String rua, String numero, String complemento, String uf) { }
