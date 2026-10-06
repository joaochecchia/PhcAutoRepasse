package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response;

import java.util.List;

public record PaginaAnunciosResponse(List<AnuncioBuscaResponse> anuncios, long total,
        int pagina, int tamanho, Integer raioKmAplicado, boolean temProximaPagina) {
    public PaginaAnunciosResponse { anuncios = List.copyOf(anuncios); }
}
