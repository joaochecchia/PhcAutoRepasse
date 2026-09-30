package repasse.phcauto.backend.anuncios.internal.core.usecase;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.anuncios.AnuncioExcluido;

import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.gateway.ExcluirAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.PublicarAlteracaoAnuncioGateway;
public final class ExcluirAnuncio implements ExcluirAnuncioUseCase {
    private final ExcluirAnuncioGateway gateway;
    private final PublicarAlteracaoAnuncioGateway eventos;
    private final Clock clock;

    public ExcluirAnuncio(ExcluirAnuncioGateway gateway, PublicarAlteracaoAnuncioGateway eventos, Clock clock) {
        this.gateway = Objects.requireNonNull(gateway);
        this.eventos = Objects.requireNonNull(eventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override public void execute(UUID anuncioId) {
        if (anuncioId == null) throw new AnuncioInvalidoException("Identificador do anúncio é obrigatório");
        UUID veiculoId = gateway.excluir(anuncioId);
        eventos.publicar(new AnuncioExcluido(UUID.randomUUID(), anuncioId, veiculoId, clock.instant()));
    }
}
