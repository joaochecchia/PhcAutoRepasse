package repasse.phcauto.backend.anuncios.internal.core;

import java.util.List;
import repasse.phcauto.backend.anuncios.AnuncioBuscaResponse;

public record PaginaAnuncios(List<AnuncioBuscaResponse> anuncios, long total,
        int pagina, int tamanho) {
    public PaginaAnuncios { anuncios = List.copyOf(anuncios); }
}
