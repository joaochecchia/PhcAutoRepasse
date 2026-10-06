package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.anuncios.ModoLocalizacaoBusca;

public record BuscarAnunciosRequest(
        ModoLocalizacaoBusca modoLocalizacao,
        TipoVeiculo tipoVeiculo,
        @Size(max = 120) String cidade,
        @Size(min = 2, max = 2) String uf,
        @Size(max = 100) String marca,
        @Size(max = 120) String modelo,
        @Size(max = 220) String termo,
        TipoPessoa tipoPessoa,
        @Size(max = 160) String perfil,
        @PositiveOrZero Long precoMinimoCentavos,
        @PositiveOrZero Long precoMaximoCentavos,
        @Positive Integer anoMinimo,
        @Positive Integer anoMaximo,
        @Size(max = 60) String cambio,
        @Size(max = 60) String combustivel,
        @Size(max = 100) String motorizacao,
        CondicaoVeiculo condicao,
        @Size(max = 60) String tipoDirecao,
        @Size(max = 40) String tracao,
        Boolean ipvaPago,
        Boolean blindado,
        @Positive Integer numeroPortas,
        @Positive BigDecimal cilindradaLitros,
        @Size(max = 60) String tipoFreio,
        @Size(max = 80) String carroceria,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Min(0) Integer pagina,
        @Min(1) @Max(52) Integer tamanho) {

    @AssertTrue(message = "Parâmetros incompatíveis com o modo de localização")
    @JsonIgnore
    public boolean isLocalizacaoConsistente() {
        boolean temCidade = cidade != null && !cidade.isBlank();
        boolean temUf = uf != null && !uf.isBlank();
        boolean temLatitude = latitude != null;
        boolean temLongitude = longitude != null;
        if (temLatitude != temLongitude) return false;
        var modo = modoEfetivo();
        return switch (modo) {
            case DISPOSITIVO -> temLatitude && temLongitude;
            case CIDADE -> temCidade && temUf && !temLatitude;
            case UF -> temUf && !temCidade && !temLatitude;
            case ENDERECO_CADASTRADO, BRASIL -> !temCidade && !temUf && !temLatitude;
        };
    }

    @JsonIgnore
    public ModoLocalizacaoBusca modoEfetivo() {
        if (modoLocalizacao != null) return modoLocalizacao;
        if (latitude != null || longitude != null) return ModoLocalizacaoBusca.DISPOSITIVO;
        if (cidade != null && !cidade.isBlank()) return ModoLocalizacaoBusca.CIDADE;
        if (uf != null && !uf.isBlank()) return ModoLocalizacaoBusca.UF;
        return ModoLocalizacaoBusca.BRASIL;
    }
}
