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

    /** Ausente para contas que usam apenas autenticação externa. Nunca é senha em texto. */
    public abstract String getSenhaHash();

    /** Celular da PF ou telefone de contato da PJ; pode faltar em cadastros legados. */
    public abstract String getTelefone();

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
