package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.internal.core.domain.PerfilPublico;
public record PerfilPublicoResponse(UUID id, String nome, TipoPessoa tipoPessoa, String cidade, String uf, String fotoUrl) {
    public static PerfilPublicoResponse from(PerfilPublico p) {
        return new PerfilPublicoResponse(p.id(), p.nome(), p.tipoPessoa(), p.cidade(), p.uf(),
                p.possuiFoto() ? "/api/v1/perfis/" + p.id() + "/foto" : null);
    }
}
