package repasse.phcauto.backend.domain.event.identidade;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

/** Fato concluído do cadastro completo; não é um comando para criar perfil/endereço. */
public record UsuarioAlterado(UUID eventId, UUID usuarioId, TipoPessoa tipoPessoa,
        Operacao operacao, Instant ocorridoEm) {
    public enum Operacao { CADASTRADO, ATUALIZADO, EXCLUIDO }

    public UsuarioAlterado {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(tipoPessoa, "tipoPessoa");
        Objects.requireNonNull(operacao, "operacao");
        Objects.requireNonNull(ocorridoEm, "ocorridoEm");
    }
}
