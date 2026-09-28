package repasse.phcauto.backend.anuncios;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

public record AnuncioCriado(UUID eventoId, UUID anuncioId, UUID veiculoId, UUID anuncianteId,
        TipoVeiculo tipoVeiculo, StatusAnuncio status, String cidade, Instant ocorridoEm) { }
