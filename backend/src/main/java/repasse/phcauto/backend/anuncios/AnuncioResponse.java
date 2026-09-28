package repasse.phcauto.backend.anuncios;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

/** Resposta pública: a localização expõe somente a cidade. */
public record AnuncioResponse(UUID id, UUID veiculoId, UUID anuncianteId, TipoVeiculo tipoVeiculo,
        String fabricante, String modelo, String versao, Integer anoFabricacao, Integer anoModelo,
        String cor, String titulo, String descricao, TipoPreco tipoPreco, Long precoCentavos,
        Boolean aceitaTroca, String cidade, StatusAnuncio status, DetalhesVeiculoResponse detalhes,
        Instant criadoEm, Instant publicadoEm) {

    public sealed interface DetalhesVeiculoResponse permits CarroResponse, MotoResponse,
            CaminhaoResponse, CaminhoneteResponse, BarcoResponse, LinhaAmarelaResponse { }
    public record CarroResponse(Integer quilometragem, String carroceria, String cambio,
            String combustivel, String tracao, String motorizacao, Integer numeroPortas,
            Integer numeroLugares, String finalPlaca, Boolean unicoDono, Boolean ipvaPago,
            Boolean licenciado, Boolean blindado) implements DetalhesVeiculoResponse { }
    public record MotoResponse(Integer quilometragem, Integer cilindradas, String categoria,
            String partida, String refrigeracao, String cambio, String combustivel, String finalPlaca,
            Boolean ipvaPago, Boolean licenciado) implements DetalhesVeiculoResponse { }
    public record CaminhaoResponse(Integer quilometragem, String configuracao, String carroceria,
            String cambio, String combustivel, String tracao, Integer numeroEixos,
            Integer capacidadeCargaKg, Integer pesoBrutoTotalKg, String implemento, String finalPlaca,
            Boolean ipvaPago, Boolean licenciado) implements DetalhesVeiculoResponse { }
    public record CaminhoneteResponse(Integer quilometragem, String tipoCabine, String carroceria,
            String cambio, String combustivel, String tracao, String motorizacao,
            Integer capacidadeCargaKg, Integer numeroPortas, String finalPlaca, Boolean unicoDono,
            Boolean ipvaPago, Boolean licenciado) implements DetalhesVeiculoResponse { }
    public record BarcoResponse(BigDecimal tamanhoPes, String estilo, String materialCasco,
            Integer capacidadePessoas, Integer numeroCabines, Integer horasUso,
            String registroMaritimo, List<MotorBarcoResponse> motores) implements DetalhesVeiculoResponse { }
    public record MotorBarcoResponse(Integer posicao, String fabricante, String modelo,
            BigDecimal potenciaHp, Integer ano, Integer horasUso, Integer horasDesdeRevisao,
            String combustivel) { }
    public record LinhaAmarelaResponse(String tipoMaquina, Integer horimetro,
            Integer pesoOperacionalKg, BigDecimal potenciaHp, String tipoEsteiraOuPneu,
            BigDecimal capacidadeCacambaM3, String numeroSerie) implements DetalhesVeiculoResponse { }
}
