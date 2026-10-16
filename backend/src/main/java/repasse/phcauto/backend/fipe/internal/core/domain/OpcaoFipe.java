package repasse.phcauto.backend.fipe.internal.core.domain;

import java.time.LocalDate;

/** A data existe somente nas opções que representam um mês de referência. */
public record OpcaoFipe(String codigo, String nome, LocalDate mesReferencia) {
    public OpcaoFipe(String codigo, String nome) {
        this(codigo, nome, null);
    }
}
