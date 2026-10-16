package repasse.phcauto.backend.fipe.internal.core;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.fipe.internal.core.domain.OpcaoFipe;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.usecase.BuscarEdicaoAtivaFipeUseCase;

class BuscarEdicaoAtivaFipeTests {
    @Test void devolveEdicaoQuePossuiMarcasParaOTipo() {
        var edicao=new OpcaoFipe("338","outubro de 2026",LocalDate.of(2026,10,1));
        var useCase=new BuscarEdicaoAtivaFipeUseCase(tipo -> "cars".equals(tipo)?Optional.of(edicao):Optional.empty());
        assertEquals(edicao,useCase.execute("cars"));
        assertThrows(FipeException.class,()->useCase.execute("motorcycles"));
    }

    @Test void rejeitaTipoDesconhecidoAntesDeConsultarCatalogo() {
        var useCase=new BuscarEdicaoAtivaFipeUseCase(tipo -> {throw new AssertionError("Não deve consultar");});
        assertThrows(FipeException.class,()->useCase.execute("boats"));
    }
}
