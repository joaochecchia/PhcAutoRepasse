package repasse.phcauto.backend.anuncios.internal.core.usecase;

import java.util.Objects;

import repasse.phcauto.backend.anuncios.internal.core.domain.BuscarAnunciosFiltro;
import repasse.phcauto.backend.anuncios.internal.core.domain.PaginaAnuncios;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosGateway;
public final class BuscarAnuncios implements BuscarAnunciosUseCase {
    private final BuscarAnunciosGateway gateway;

    public BuscarAnuncios(BuscarAnunciosGateway gateway) { this.gateway = Objects.requireNonNull(gateway); }

    @Override
    public PaginaAnuncios execute(BuscarAnunciosFiltro f) {
        if (f == null) throw new AnuncioInvalidoException("Filtros são obrigatórios");
        if (f.precoMinimoCentavos() != null && f.precoMaximoCentavos() != null
                && f.precoMinimoCentavos() > f.precoMaximoCentavos())
            throw new AnuncioInvalidoException("Preço mínimo não pode ser maior que o máximo");
        if (f.anoMinimo() != null && f.anoMaximo() != null && f.anoMinimo() > f.anoMaximo())
            throw new AnuncioInvalidoException("Ano mínimo não pode ser maior que o máximo");
        if (f.pagina() < 0 || f.tamanho() < 1 || f.tamanho() > 100)
            throw new AnuncioInvalidoException("Paginação inválida");
        return gateway.buscar(f);
    }
}
