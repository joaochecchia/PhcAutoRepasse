package repasse.phcauto.backend.domain.model.catalogo;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de catalogo.anuncios.
 * Sem dependência de persistência ou framework.
 */
public abstract class Anuncio {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVeiculoId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getAnuncianteId();

    /** Obrigatório no modelo inicial. */
    public abstract String getTitulo();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getDescricao();

    /** Obrigatório no modelo inicial. */
    public abstract TipoPreco getTipoPreco();

    /** Obrigatório para FIXO; deve retornar null para SOB_CONSULTA. */
    public abstract Long getPrecoCentavos();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Boolean getAceitaTroca();

    /** Obrigatório no modelo inicial. */
    public abstract String getCidade();

    /** Obrigatório no modelo inicial. */
    public abstract String getUf();

    /** Obrigatório no modelo inicial. */
    public abstract StatusAnuncio getStatus();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getPublicadoEm();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getAtualizadoEm();

    /** Obrigatório no modelo inicial. */
    public abstract int getVersao();
}
