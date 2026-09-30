package repasse.phcauto.backend.anuncios.internal.core.gateway;

import java.time.Instant;
import java.util.UUID;

import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioCriadoResultado;
import repasse.phcauto.backend.anuncios.internal.core.domain.AtualizarAnuncioCommand;
public interface AtualizarAnuncioGateway {
    AnuncioCriadoResultado atualizar(UUID anuncioId, AtualizarAnuncioCommand alteracoes, Instant agora);
}
