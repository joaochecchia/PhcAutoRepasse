package repasse.phcauto.backend.usuarios.internal.core.domain;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
public record PerfilPublico(UUID id, String nome, TipoPessoa tipoPessoa, String cidade, String uf, boolean possuiFoto) { }
