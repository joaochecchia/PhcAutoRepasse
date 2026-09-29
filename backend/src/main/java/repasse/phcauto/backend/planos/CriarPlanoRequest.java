package repasse.phcauto.backend.planos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CriarPlanoRequest(
        @NotBlank @Size(max = 100) String nome,
        @PositiveOrZero Long valorCentavos,
        @Min(1) @Max(32767) Integer periodoMeses,
        @PositiveOrZero Integer limiteAnuncios,
        @PositiveOrZero Integer limiteVistoriasCautelares,
        boolean ativo) { }
