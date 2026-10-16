package repasse.phcauto.backend.fipe.internal.core.domain;

import java.time.LocalDate;

/**
 * A data existe somente nas opções que representam um mês de referência. {@code anoId} (ex.: 2020-5) existe somente
 * nos modelos listados por ano: identifica o ano/combustível necessário para consultar o preço daquele modelo.
 */
public record OpcaoFipe(String codigo, String nome, LocalDate mesReferencia, String anoId) {
    public OpcaoFipe(String codigo, String nome, LocalDate mesReferencia) {
        this(codigo, nome, mesReferencia, null);
    }
    public OpcaoFipe(String codigo, String nome) {
        this(codigo, nome, null, null);
    }
}
