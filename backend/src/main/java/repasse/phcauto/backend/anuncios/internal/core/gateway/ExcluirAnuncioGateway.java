package repasse.phcauto.backend.anuncios.internal.core.gateway;

import java.util.UUID;

public interface ExcluirAnuncioGateway {
    UUID excluir(UUID anuncioId);
}
