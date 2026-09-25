package repasse.phcauto.backend.infra.database.sync;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Evento técnico de invalidação; não transporta dados pessoais nem snapshots antigos. */
public record RowChanged(UUID eventId, ProjectionTable table, List<String> key) {
    public RowChanged {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(table);
        key = List.copyOf(key);
        if (key.size() != table.keys.size()) throw new IllegalArgumentException("Chave inválida");
    }
    public static RowChanged of(ProjectionTable table, List<String> key) {
        return new RowChanged(UUID.randomUUID(), table, key);
    }
}
