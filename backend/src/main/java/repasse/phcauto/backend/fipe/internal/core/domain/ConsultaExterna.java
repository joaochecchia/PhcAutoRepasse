package repasse.phcauto.backend.fipe.internal.core.domain;
import java.time.LocalDate;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import static repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.INVALIDA;
public record ConsultaExterna(String tipo, String marcaId, String modeloId, String anoId,
        int referencia, LocalDate mesReferencia) {
    public ConsultaExterna {
        if (!java.util.Set.of("cars", "motorcycles", "trucks").contains(tipo == null ? "" : tipo)
                || marcaId == null || !marcaId.matches("[0-9]{1,10}")
                || modeloId == null || !modeloId.matches("[0-9]{1,10}")
                || anoId == null || !anoId.matches("[0-9]{4,5}-[0-9]") || referencia < 1
                || mesReferencia == null || mesReferencia.getDayOfMonth() != 1)
            throw new FipeException(INVALIDA, "Identificadores da consulta FIPE inválidos");
    }
    public String chave() { return tipo + ":" + marcaId + ":" + modeloId + ":" + anoId + ":" + referencia; }
}
