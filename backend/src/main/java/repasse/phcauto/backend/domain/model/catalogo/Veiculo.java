package repasse.phcauto.backend.domain.model.catalogo;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de catalogo.veiculos.
 * Sem dependência de persistência ou framework.
 */
public abstract class Veiculo {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getProprietarioId();

    /** Obrigatório no modelo inicial. */
    public abstract TipoVeiculo getTipo();

    /** Obrigatório no modelo inicial. */
    public abstract String getFabricante();

    /** Obrigatório no modelo inicial. */
    public abstract String getModelo();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getVersao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getAnoFabricacao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getAnoModelo();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCor();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getIdentificadorPublico();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getAtualizadoEm();
}
