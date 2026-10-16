package repasse.phcauto.backend.domain.model.catalogo;
import java.time.LocalDate;
import java.util.UUID;
/** Snapshot imutável da referência escolhida; não é atualizado silenciosamente a cada mês. */
public record FipeVeiculo(UUID referenciaId, String codigo, long valorCentavos,
        LocalDate mesReferencia, String modelo, String fonte) {
    public FipeVeiculo {
        if(referenciaId==null || codigo==null || valorCentavos<=0 || mesReferencia==null || modelo==null || fonte==null)
            throw new IllegalArgumentException("Snapshot FIPE incompleto");
    }
    public java.math.BigDecimal diferencaPercentual(Long preco) {
        if(preco==null)return null;
        return java.math.BigDecimal.valueOf(preco).subtract(java.math.BigDecimal.valueOf(valorCentavos))
            .multiply(java.math.BigDecimal.valueOf(100)).divide(java.math.BigDecimal.valueOf(valorCentavos),2,java.math.RoundingMode.HALF_UP);
    }
}
