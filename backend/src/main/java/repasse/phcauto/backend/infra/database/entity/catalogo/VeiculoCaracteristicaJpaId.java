package repasse.phcauto.backend.infra.database.entity.catalogo;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record VeiculoCaracteristicaJpaId(
        @Column(name = "veiculo_id", nullable = false) UUID veiculoId,
        @Column(name = "caracteristica_id", nullable = false) UUID caracteristicaId) implements Serializable {
    public VeiculoCaracteristicaJpaId {
        Objects.requireNonNull(veiculoId, "veiculoId");
        Objects.requireNonNull(caracteristicaId, "caracteristicaId");
    }
}
