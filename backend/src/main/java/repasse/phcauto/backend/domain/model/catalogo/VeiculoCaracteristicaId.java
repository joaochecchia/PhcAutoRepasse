package repasse.phcauto.backend.domain.model.catalogo;

import java.util.Objects;
import java.util.UUID;

/** Chave composta da associação entre veículo e característica. */
public record VeiculoCaracteristicaId(UUID veiculoId, UUID caracteristicaId) {

    public VeiculoCaracteristicaId {
        Objects.requireNonNull(veiculoId, "veiculoId é obrigatório");
        Objects.requireNonNull(caracteristicaId, "caracteristicaId é obrigatório");
    }
}
