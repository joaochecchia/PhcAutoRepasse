package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.*;
import repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe;

public record BuscarSugestoesFipeRequest(
        @NotBlank @Size(max=20) String tipoVeiculo,
        @NotBlank @Size(max=100) String fabricante,
        @NotBlank @Size(max=120) String modelo,
        @NotBlank @Size(max=180) String versao,
        @Min(1886) @Max(2200) int anoModelo,
        @NotBlank @Size(max=20) String condicao,
        @NotBlank @Size(max=60) String combustivel) {
    public BuscarSugestoesFipe comando() {
        return new BuscarSugestoesFipe(tipoVeiculo, fabricante, modelo, versao,
                anoModelo, condicao, combustivel);
    }
}
