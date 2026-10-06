package repasse.phcauto.backend.anuncios.internal.core.domain;

import java.util.List;

public record PaginaAnuncios(List<AnuncioResumo> anuncios, long total,
        int pagina, int tamanho, Integer raioKmAplicado, boolean temProximaPagina) {
    public PaginaAnuncios { anuncios = List.copyOf(anuncios); }
}
