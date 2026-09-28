package repasse.phcauto.backend.anuncios.internal.core;

import java.time.Instant;
import java.util.UUID;

public interface AtualizarAnuncioGateway {
    AnuncioCriadoResultado atualizar(UUID anuncioId, AtualizarAnuncioCommand alteracoes, Instant agora);
}
