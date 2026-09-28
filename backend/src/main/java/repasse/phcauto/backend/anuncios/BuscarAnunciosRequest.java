package repasse.phcauto.backend.anuncios;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record BuscarAnunciosRequest(
        TipoVeiculo tipoVeiculo,
        @Size(max = 120) String cidade,
        @Size(min = 2, max = 2) String uf,
        @Size(max = 100) String marca,
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
        @Min(0) Integer pagina,
        @Min(1) @Max(100) Integer tamanho) { }
