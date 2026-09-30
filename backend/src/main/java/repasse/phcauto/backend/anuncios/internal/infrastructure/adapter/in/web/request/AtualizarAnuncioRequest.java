package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import jakarta.validation.constraints.Size;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;

/** Campos ausentes permanecem inalterados. O tipo e o anunciante do veículo não podem ser trocados. */
public record AtualizarAnuncioRequest(
        @Size(min = 1, max = 100) String fabricante,
        @Size(min = 1, max = 120) String modelo,
        @Size(max = 180) String versao,
        @Positive Integer anoFabricacao,
        @Positive Integer anoModelo,
        @Size(max = 60) String cor,
        @Size(max = 80) String identificadorPublico,
        repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo condicao,
        @Size(max = 60) String tipoFreio,
        @Size(min = 1, max = 180) String titulo,
        @Size(max = 10000) String descricao,
        TipoPreco tipoPreco,
        @Positive Long precoCentavos,
        Boolean aceitaTroca,
        Boolean publicarAgora,
        @Valid EnderecoAnuncioPatchRequest endereco,
        @Valid CarroPatchRequest carro,
        @Valid MotoPatchRequest moto,
        @Valid CaminhaoPatchRequest caminhao,
        @Valid CaminhonetePatchRequest caminhonete,
        @Valid CriarAnuncioRequest.BarcoRequest barco,
        @Valid CriarAnuncioRequest.LinhaAmarelaRequest linhaAmarela) {

    public record CarroPatchRequest(@PositiveOrZero Integer quilometragem, @Size(max=60) String carroceria,
            @Size(max=60) String cambio, @Size(max=60) String combustivel, @Size(max=40) String tracao,
            @Size(max=100) String motorizacao, @Size(max=60) String tipoDirecao,
            @Positive BigDecimal cilindradaLitros, @Positive Integer numeroPortas, @Positive Integer numeroLugares,
            @Pattern(regexp="(?i)[A-Z]{3}[0-9][A-Z0-9][0-9]{2}") String placa,
            Boolean exibirPlacaCompleta, Boolean unicoDono, Boolean ipvaPago, Boolean licenciado, Boolean blindado) { }

    public record MotoPatchRequest(@PositiveOrZero Integer quilometragem, @Positive Integer cilindradas,
            @Size(max=60) String categoria, @Size(max=40) String partida, @Size(max=40) String refrigeracao,
            @Size(max=60) String cambio, @Size(max=60) String combustivel,
            @Pattern(regexp="(?i)[A-Z]{3}[0-9][A-Z0-9][0-9]{2}") String placa,
            Boolean exibirPlacaCompleta, Boolean ipvaPago, Boolean licenciado) { }

    public record CaminhaoPatchRequest(@PositiveOrZero Integer quilometragem, @Size(max=80) String configuracao,
            @Size(max=80) String carroceria, @Size(max=60) String cambio, @Size(max=60) String combustivel,
            @Size(max=40) String tracao, @Size(max=60) String tipoDirecao, @Positive Integer numeroEixos,
            @Positive Integer capacidadeCargaKg, @Positive Integer pesoBrutoTotalKg, @Size(max=100) String implemento,
            @Pattern(regexp="(?i)[A-Z]{3}[0-9][A-Z0-9][0-9]{2}") String placa,
            Boolean exibirPlacaCompleta, Boolean ipvaPago, Boolean licenciado) { }

    public record CaminhonetePatchRequest(@PositiveOrZero Integer quilometragem, @Size(max=50) String tipoCabine,
            @Size(max=60) String carroceria, @Size(max=60) String cambio, @Size(max=60) String combustivel,
            @Size(max=40) String tracao, @Size(max=100) String motorizacao, @Size(max=60) String tipoDirecao,
            @Positive BigDecimal cilindradaLitros, @Positive Integer capacidadeCargaKg, @Positive Integer numeroPortas,
            @Pattern(regexp="(?i)[A-Z]{3}[0-9][A-Z0-9][0-9]{2}") String placa,
            Boolean exibirPlacaCompleta, Boolean unicoDono, Boolean ipvaPago, Boolean licenciado, Boolean blindado) { }

    @jakarta.validation.constraints.AssertTrue(message = "Informe no máximo um bloco de dados específicos")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isApenasUmBlocoDeDetalhes() {
        return (carro != null ? 1 : 0) + (moto != null ? 1 : 0)
                + (caminhao != null ? 1 : 0) + (caminhonete != null ? 1 : 0)
                + (barco != null ? 1 : 0) + (linhaAmarela != null ? 1 : 0) <= 1;
    }
    @jakarta.validation.constraints.AssertTrue(message = "Informe ao menos um campo para atualizar")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isPossuiAlgumaAlteracao() {
        return fabricante != null || modelo != null || versao != null || anoFabricacao != null
                || anoModelo != null || cor != null || identificadorPublico != null || condicao != null
                || tipoFreio != null || titulo != null || descricao != null || tipoPreco != null
                || precoCentavos != null || aceitaTroca != null || publicarAgora != null
                || possuiValor(endereco) || possuiValor(carro) || possuiValor(moto)
                || possuiValor(caminhao) || possuiValor(caminhonete)
                || possuiValor(barco) || possuiValor(linhaAmarela);
    }

    private static boolean possuiValor(Object registro) {
        if (registro == null) return false;
        try {
            for (var componente : registro.getClass().getRecordComponents()) {
                if (componente.getAccessor().invoke(registro) != null) return true;
            }
            return false;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Não foi possível validar a atualização", ex);
        }
    }
}
