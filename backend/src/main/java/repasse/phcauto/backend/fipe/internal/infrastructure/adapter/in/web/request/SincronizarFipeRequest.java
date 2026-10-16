package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.request;
import java.time.LocalDate;
import repasse.phcauto.backend.fipe.internal.core.domain.SincronizarFipeCommand;
public record SincronizarFipeRequest(String tipo,String marcaId,String modeloId,String anoId,Integer referencia,LocalDate mesReferencia) {
    public SincronizarFipeCommand comando(){return new SincronizarFipeCommand(tipo,marcaId,modeloId,anoId,referencia,mesReferencia);}
}
