package repasse.phcauto.backend.anuncios.internal.core.usecase;

import java.util.UUID;

import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioCriadoResultado;
import repasse.phcauto.backend.anuncios.internal.core.domain.AtualizarAnuncioCommand;
public interface AtualizarAnuncioUseCase {
    AnuncioCriadoResultado execute(UUID anuncioId, AtualizarAnuncioCommand alteracoes);
}
