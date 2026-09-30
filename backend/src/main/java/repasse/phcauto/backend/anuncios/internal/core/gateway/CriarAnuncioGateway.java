package repasse.phcauto.backend.anuncios.internal.core.gateway;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;

import repasse.phcauto.backend.anuncios.internal.core.domain.CriarAnuncioCommand;
public interface CriarAnuncioGateway {
    boolean usuarioAtivo(UUID usuarioId);
    void salvar(UUID anuncioId, UUID veiculoId, UUID enderecoId, CriarAnuncioCommand dados,
            StatusAnuncio status, Instant agora);
}
