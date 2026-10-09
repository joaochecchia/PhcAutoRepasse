package repasse.phcauto.backend.anuncios.internal.core.usecase;

import java.util.List;
import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioResumo;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosPaginaInicialGateway;

public final class BuscarAnunciosPaginaInicial implements BuscarAnunciosPaginaInicialUseCase {
    private static final int LIMITE_MAXIMO = 20;
    private final BuscarAnunciosPaginaInicialGateway gateway;

    public BuscarAnunciosPaginaInicial(BuscarAnunciosPaginaInicialGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public List<AnuncioResumo> execute(int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("O limite da página inicial deve estar entre 1 e 20");
        }
        return List.copyOf(gateway.buscar(limite));
    }
}
