package repasse.phcauto.backend.planos.internal.core.domain;

import java.time.Instant;
import java.util.UUID;

public record PlanoDados(UUID id, String nome, Long valorCentavos, Integer periodoMeses,
        Integer limiteAnuncios, Integer limiteVistoriasCautelares,
        boolean ativo, Instant criadoEm) { }
