package repasse.phcauto.backend.planos.internal.infrastructure.adapter.in.web.response;

import java.time.Instant;
import java.util.UUID;

public record PlanoResponse(UUID id, String nome, Long valorCentavos, Integer periodoMeses,
        Integer limiteAnuncios, Integer limiteVistoriasCautelares,
        boolean ativo, Instant criadoEm) { }
