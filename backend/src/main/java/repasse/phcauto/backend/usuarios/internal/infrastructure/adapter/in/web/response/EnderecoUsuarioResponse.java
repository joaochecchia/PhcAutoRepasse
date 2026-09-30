package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response;

public record EnderecoUsuarioResponse(String cep, String cidade, String bairro,
        String rua, String numero, String complemento, String uf) { }
