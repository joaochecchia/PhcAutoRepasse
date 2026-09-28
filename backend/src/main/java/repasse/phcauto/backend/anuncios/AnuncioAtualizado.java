package repasse.phcauto.backend.anuncios;

import java.time.Instant;
import java.util.UUID;

public record AnuncioAtualizado(UUID eventoId, UUID anuncioId, Instant ocorridoEm) { }
