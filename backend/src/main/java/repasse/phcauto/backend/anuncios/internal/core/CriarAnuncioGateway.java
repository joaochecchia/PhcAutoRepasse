package repasse.phcauto.backend.anuncios.internal.core;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;

public interface CriarAnuncioGateway {
    boolean usuarioAtivo(UUID usuarioId);
    void salvar(UUID anuncioId, UUID veiculoId, UUID enderecoId, CriarAnuncioCommand dados,
            StatusAnuncio status, Instant agora);
}
