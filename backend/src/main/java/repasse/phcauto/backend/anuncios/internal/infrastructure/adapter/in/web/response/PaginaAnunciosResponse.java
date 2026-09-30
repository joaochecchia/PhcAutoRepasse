package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response;

import java.util.List;

public record PaginaAnunciosResponse(List<AnuncioBuscaResponse> anuncios, long total,
        int pagina, int tamanho) {
    public PaginaAnunciosResponse { anuncios = List.copyOf(anuncios); }
}
