package repasse.phcauto.backend.anuncios.internal.core;

import java.util.UUID;

public interface AtualizarAnuncioUseCase {
    AnuncioCriadoResultado execute(UUID anuncioId, AtualizarAnuncioCommand alteracoes);
}
