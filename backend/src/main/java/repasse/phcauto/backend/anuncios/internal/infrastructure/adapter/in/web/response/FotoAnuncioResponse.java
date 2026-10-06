package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response;

import java.util.UUID;

public record FotoAnuncioResponse(UUID id, UUID anuncioId, int posicao, String textoAlternativo, String url) { }
