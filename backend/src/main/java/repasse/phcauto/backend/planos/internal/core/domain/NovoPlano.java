package repasse.phcauto.backend.planos.internal.core.domain;

public record NovoPlano(String nome, Long valorCentavos, Integer periodoMeses,
        Integer limiteAnuncios, Integer limiteVistoriasCautelares, boolean ativo) { }
