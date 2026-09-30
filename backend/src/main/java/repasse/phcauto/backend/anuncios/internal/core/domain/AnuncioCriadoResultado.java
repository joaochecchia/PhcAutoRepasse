package repasse.phcauto.backend.anuncios.internal.core.domain;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;

public record AnuncioCriadoResultado(UUID anuncioId, UUID veiculoId, CriarAnuncioCommand dados,
        StatusAnuncio status, Instant criadoEm, Instant publicadoEm) { }
