package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request;

public record EnderecoPatchRequest(
        String cep,
        String cidade,
        String bairro,
        String rua,
        String numero,
        String complemento,
        String uf) { }
