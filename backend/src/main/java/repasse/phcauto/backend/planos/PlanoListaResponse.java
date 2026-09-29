package repasse.phcauto.backend.planos;

import java.util.List;

public record PlanoListaResponse(List<PlanoResponse> planos, long total, int offset, int limite) {
    public PlanoListaResponse {
        planos = List.copyOf(planos);
    }
}
