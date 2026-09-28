package repasse.phcauto.backend.anuncios.internal.core;

import java.util.UUID;

public interface ExcluirAnuncioUseCase {
    void execute(UUID anuncioId);
}
