package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request;
import jakarta.validation.constraints.*;
import repasse.phcauto.backend.usuarios.internal.core.domain.FiltroLojistas;
public record BuscarLojistasRequest(@Size(max=160) String nome, @Size(max=120) String cidade,
        @Pattern(regexp="(?i)[a-z]{2}") String uf,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Min(0) Integer pagina, @Min(1) @Max(12) Integer tamanho) {
    public FiltroLojistas filtro() {
        return new FiltroLojistas(nome, cidade, uf, latitude, longitude,
                pagina == null ? 0 : pagina, tamanho == null ? 12 : tamanho);
    }
}
