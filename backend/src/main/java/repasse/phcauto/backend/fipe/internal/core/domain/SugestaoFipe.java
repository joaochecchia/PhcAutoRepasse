package repasse.phcauto.backend.fipe.internal.core.domain;

import java.time.LocalDate;

public record SugestaoFipe(String tipo, String marcaId, String modeloId, String anoId,
        int referencia, LocalDate mesReferencia, String marca, String modelo, String anoCombustivel) { }
