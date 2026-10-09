package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response;

import java.util.List;

public record PaginaInicialAnunciosResponse(String mensagem, List<AnuncioBuscaResponse> carros) {
    public PaginaInicialAnunciosResponse {
        carros = List.copyOf(carros);
    }
}
