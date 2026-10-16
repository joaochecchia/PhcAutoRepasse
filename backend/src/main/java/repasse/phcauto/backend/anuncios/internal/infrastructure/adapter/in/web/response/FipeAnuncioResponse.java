package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.FipeVeiculo;
public record FipeAnuncioResponse(UUID referenciaId,String codigo,long valorCentavos,LocalDate mesReferencia,
        String modelo,String fonte,Long diferencaCentavos,BigDecimal diferencaPercentual) {
    public static FipeAnuncioResponse de(FipeVeiculo f,Long preco) {
        return f==null?null:new FipeAnuncioResponse(f.referenciaId(),f.codigo(),f.valorCentavos(),f.mesReferencia(),f.modelo(),f.fonte(),
                preco==null?null:preco-f.valorCentavos(),f.diferencaPercentual(preco));
    }
}
