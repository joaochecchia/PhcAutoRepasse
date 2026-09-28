package repasse.phcauto.backend.anuncios;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

public record CriarAnuncioRequest(
        @NotNull UUID anuncianteId,
        @NotNull TipoVeiculo tipoVeiculo,
        @NotBlank @Size(max = 100) String fabricante,
        @NotBlank @Size(max = 120) String modelo,
        @Size(max = 180) String versao,
        @Positive Integer anoFabricacao,
        @Positive Integer anoModelo,
        @Size(max = 60) String cor,
        @Size(max = 80) String identificadorPublico,
        @NotBlank @Size(max = 180) String titulo,
        @Size(max = 10000) String descricao,
        @NotNull TipoPreco tipoPreco,
        @Positive Long precoCentavos,
        Boolean aceitaTroca,
        Boolean publicarAgora,
        @Valid @NotNull EnderecoAnuncioRequest endereco,
        @Valid CarroRequest carro,
        @Valid MotoRequest moto,
        @Valid CaminhaoRequest caminhao,
        @Valid CaminhoneteRequest caminhonete,
        @Valid BarcoRequest barco,
        @Valid LinhaAmarelaRequest linhaAmarela) {

    public record CarroRequest(@PositiveOrZero Integer quilometragem, @Size(max=60) String carroceria,
            @Size(max=60) String cambio, @Size(max=60) String combustivel, @Size(max=40) String tracao,
            @Size(max=100) String motorizacao, @Positive Integer numeroPortas, @Positive Integer numeroLugares,
            @Pattern(regexp="[A-Za-z0-9]") String finalPlaca, Boolean unicoDono, Boolean ipvaPago,
            Boolean licenciado, Boolean blindado) { }

    public record MotoRequest(@PositiveOrZero Integer quilometragem, @Positive Integer cilindradas,
            @Size(max=60) String categoria, @Size(max=40) String partida, @Size(max=40) String refrigeracao,
            @Size(max=60) String cambio, @Size(max=60) String combustivel,
            @Pattern(regexp="[A-Za-z0-9]") String finalPlaca, Boolean ipvaPago, Boolean licenciado) { }

    public record CaminhaoRequest(@PositiveOrZero Integer quilometragem, @Size(max=80) String configuracao,
            @Size(max=80) String carroceria, @Size(max=60) String cambio, @Size(max=60) String combustivel,
            @Size(max=40) String tracao, @Positive Integer numeroEixos, @Positive Integer capacidadeCargaKg,
            @Positive Integer pesoBrutoTotalKg, @Size(max=100) String implemento,
            @Pattern(regexp="[A-Za-z0-9]") String finalPlaca, Boolean ipvaPago, Boolean licenciado) { }

    public record CaminhoneteRequest(@PositiveOrZero Integer quilometragem, @Size(max=50) String tipoCabine,
            @Size(max=60) String carroceria, @Size(max=60) String cambio, @Size(max=60) String combustivel,
            @Size(max=40) String tracao, @Size(max=100) String motorizacao,
            @Positive Integer capacidadeCargaKg, @Positive Integer numeroPortas,
            @Pattern(regexp="[A-Za-z0-9]") String finalPlaca, Boolean unicoDono, Boolean ipvaPago,
            Boolean licenciado) { }

    public record BarcoRequest(@Positive BigDecimal tamanhoPes, @Size(max=80) String estilo,
            @Size(max=80) String materialCasco, @Positive Integer capacidadePessoas,
            @PositiveOrZero Integer numeroCabines, @PositiveOrZero Integer horasUso,
            @Size(max=80) String registroMaritimo, @Valid List<MotorBarcoRequest> motores) {
        public BarcoRequest { motores = motores == null ? List.of() : List.copyOf(motores); }
    }

    public record MotorBarcoRequest(@Positive Integer posicao, @Size(max=100) String fabricante,
            @Size(max=120) String modelo, @Positive BigDecimal potenciaHp, @Positive Integer ano,
            @PositiveOrZero Integer horasUso, @PositiveOrZero Integer horasDesdeRevisao,
            @Size(max=60) String combustivel) { }

    public record LinhaAmarelaRequest(@Size(max=80) String tipoMaquina, @PositiveOrZero Integer horimetro,
            @Positive Integer pesoOperacionalKg, @Positive BigDecimal potenciaHp,
            @Size(max=30) String tipoEsteiraOuPneu, @Positive BigDecimal capacidadeCacambaM3,
            @Size(max=100) String numeroSerie) { }
}
