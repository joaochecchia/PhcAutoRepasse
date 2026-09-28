package repasse.phcauto.backend.anuncios.internal.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

public record CriarAnuncioCommand(UUID anuncianteId, TipoVeiculo tipoVeiculo, String fabricante,
        String modelo, String versao, Integer anoFabricacao, Integer anoModelo, String cor,
        String identificadorPublico, String titulo, String descricao, TipoPreco tipoPreco,
        Long precoCentavos, Boolean aceitaTroca, boolean publicarAgora, Endereco endereco,
        DetalhesVeiculo detalhes) {

    public record Endereco(String cep, String cidade, String bairro, String rua, String numero,
            String complemento, String uf) { }

    public sealed interface DetalhesVeiculo permits Carro, Moto, Caminhao, Caminhonete, Barco, LinhaAmarela { }

    public record Carro(Integer quilometragem, String carroceria, String cambio, String combustivel,
            String tracao, String motorizacao, Integer numeroPortas, Integer numeroLugares,
            String finalPlaca, Boolean unicoDono, Boolean ipvaPago, Boolean licenciado,
            Boolean blindado) implements DetalhesVeiculo { }

    public record Moto(Integer quilometragem, Integer cilindradas, String categoria, String partida,
            String refrigeracao, String cambio, String combustivel, String finalPlaca,
            Boolean ipvaPago, Boolean licenciado) implements DetalhesVeiculo { }

    public record Caminhao(Integer quilometragem, String configuracao, String carroceria, String cambio,
            String combustivel, String tracao, Integer numeroEixos, Integer capacidadeCargaKg,
            Integer pesoBrutoTotalKg, String implemento, String finalPlaca, Boolean ipvaPago,
            Boolean licenciado) implements DetalhesVeiculo { }

    public record Caminhonete(Integer quilometragem, String tipoCabine, String carroceria, String cambio,
            String combustivel, String tracao, String motorizacao, Integer capacidadeCargaKg,
            Integer numeroPortas, String finalPlaca, Boolean unicoDono, Boolean ipvaPago,
            Boolean licenciado) implements DetalhesVeiculo { }

    public record Barco(BigDecimal tamanhoPes, String estilo, String materialCasco,
            Integer capacidadePessoas, Integer numeroCabines, Integer horasUso,
            String registroMaritimo, List<Motor> motores) implements DetalhesVeiculo {
        public Barco { motores = motores == null ? List.of() : List.copyOf(motores); }
    }

    public record Motor(Integer posicao, String fabricante, String modelo, BigDecimal potenciaHp,
            Integer ano, Integer horasUso, Integer horasDesdeRevisao, String combustivel) { }

    public record LinhaAmarela(String tipoMaquina, Integer horimetro, Integer pesoOperacionalKg,
            BigDecimal potenciaHp, String tipoEsteiraOuPneu, BigDecimal capacidadeCacambaM3,
            String numeroSerie) implements DetalhesVeiculo { }
}
