package repasse.phcauto.backend.usuarios.internal.core.domain;

public record EnderecoUsuarioDados(String cep, String cidade, String bairro,
        String rua, String numero, String complemento, String uf) { }
