package repasse.phcauto.backend.anuncios;

import java.time.Instant;
import java.util.UUID;

public record AnuncioExcluido(UUID eventoId, UUID anuncioId, UUID veiculoId, Instant ocorridoEm) { }
