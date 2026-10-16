package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response;

import java.time.LocalDate;
import repasse.phcauto.backend.fipe.internal.core.domain.SugestaoFipe;

public record SugestaoFipeResponse(String tipo, String marcaId, String modeloId, String anoId,
        int referencia, LocalDate mesReferencia, String marca, String modelo, String anoCombustivel) {
    public static SugestaoFipeResponse de(SugestaoFipe s) {
        return new SugestaoFipeResponse(s.tipo(), s.marcaId(), s.modeloId(), s.anoId(),
                s.referencia(), s.mesReferencia(), s.marca(), s.modelo(), s.anoCombustivel());
    }
}
