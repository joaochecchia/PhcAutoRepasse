package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.request;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaExterna;
public record ResolverFipeRequest(@NotBlank String tipo,@NotBlank String marcaId,@NotBlank String modeloId,
        @NotBlank String anoId,@Positive int referencia,@NotNull LocalDate mesReferencia){
    public ConsultaExterna comando(){return new ConsultaExterna(tipo,marcaId,modeloId,anoId,referencia,mesReferencia);}
}
