package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response;
import java.time.LocalDate;
import java.util.UUID;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
public record ReferenciaFipeResponse(UUID id,String codigoFipe,String tipo,String marca,String modelo,
        int anoModelo,boolean zeroKm,String combustivel,LocalDate mesReferencia,long valorCentavos,String fonte){
    public static ReferenciaFipeResponse de(ReferenciaFipe r){return new ReferenciaFipeResponse(r.id(),r.codigoFipe(),r.tipo(),r.marca(),r.modelo(),r.anoModelo(),r.zeroKm(),r.combustivel(),r.mesReferencia(),r.valorCentavos(),r.fonte());}
}
