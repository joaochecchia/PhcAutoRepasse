package repasse.phcauto.backend.domain.model.identidade;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de identidade.usuarios.
 * Sem dependência de persistência ou framework.
 */
public abstract class Usuario {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract String getNome();

    /** Obrigatório no modelo inicial. */
    public abstract String getEmail();

    /** Obrigatório no modelo inicial. */
    public abstract String getSenhaHash();

    /** Obrigatório no modelo inicial. */
    public abstract TipoPessoa getTipoPessoa();

    /** Obrigatório no modelo inicial. */
    public abstract PapelUsuario getPapel();

    /** Obrigatório no modelo inicial. */
    public abstract boolean getAtivo();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getAtualizadoEm();
}
