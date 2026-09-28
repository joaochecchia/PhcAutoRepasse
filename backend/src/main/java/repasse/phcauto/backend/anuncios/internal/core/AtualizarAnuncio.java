package repasse.phcauto.backend.anuncios.internal.core;

import java.time.Clock;
import java.time.Year;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.anuncios.AnuncioAtualizado;

public final class AtualizarAnuncio implements AtualizarAnuncioUseCase {
    private final AtualizarAnuncioGateway gateway;
    private final PublicarAlteracaoAnuncioGateway eventos;
    private final Clock clock;

    public AtualizarAnuncio(AtualizarAnuncioGateway gateway, PublicarAlteracaoAnuncioGateway eventos, Clock clock) {
        this.gateway = Objects.requireNonNull(gateway);
        this.eventos = Objects.requireNonNull(eventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override public AnuncioCriadoResultado execute(UUID anuncioId, AtualizarAnuncioCommand c) {
        if (anuncioId == null || c == null) throw new AnuncioInvalidoException("Anúncio e alterações são obrigatórios");
        validarAno(c.anoFabricacao(), "anoFabricacao");
        validarAno(c.anoModelo(), "anoModelo");
        var agora = clock.instant();
        var resultado = gateway.atualizar(anuncioId, c, agora);
        eventos.publicar(new AnuncioAtualizado(UUID.randomUUID(), anuncioId, agora));
        return resultado;
    }

    private void validarAno(Integer ano, String campo) {
        if (ano != null && (ano < 1886 || ano > Year.now(clock).getValue() + 1))
            throw new AnuncioInvalidoException(campo + " inválido");
    }
}
