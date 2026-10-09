package repasse.phcauto.backend.anuncios.internal.core;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.AnuncioCriado;
import repasse.phcauto.backend.domain.model.catalogo.*;

import repasse.phcauto.backend.anuncios.internal.core.domain.CriarAnuncioCommand;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncianteInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.gateway.CriarAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.usecase.CriarAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
class CriarAnuncioTests {
    private static final UUID USUARIO = UUID.randomUUID();
    private final Gateway gateway = new Gateway();
    private final List<AnuncioCriado> eventos = new ArrayList<>();
    private final CriarAnuncio useCase = new CriarAnuncio(gateway, eventos::add,
            Clock.fixed(Instant.parse("2026-09-28T12:00:00Z"), ZoneOffset.UTC));

    @ParameterizedTest
    @EnumSource(TipoVeiculo.class)
    void criaTodosOsTiposNaMesmaOperacao(TipoVeiculo tipo) {
        var resultado = useCase.execute(command(tipo, detalhes(tipo), true, TipoPreco.FIXO, 100_000L));
        assertThat(resultado.status()).isEqualTo(StatusAnuncio.PUBLICADO);
        assertThat(resultado.publicadoEm()).isEqualTo(Instant.parse("2026-09-28T12:00:00Z"));
        assertThat(gateway.salvamentos).isEqualTo(1);
        assertThat(eventos).singleElement().satisfies(e -> {
            assertThat(e.anuncioId()).isEqualTo(resultado.anuncioId());
            assertThat(e.tipoVeiculo()).isEqualTo(tipo);
            assertThat(e.cidade()).isEqualTo("Goiânia");
        });
    }

    @Test void criaRascunhoSemDataDePublicacao() {
        var r = useCase.execute(command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.CARRO), false, TipoPreco.SOB_CONSULTA, null));
        assertThat(r.status()).isEqualTo(StatusAnuncio.RASCUNHO);
        assertThat(r.publicadoEm()).isNull();
    }

    @Test void rejeitaDetalhesDeOutroTipo() {
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.MOTO), true, TipoPreco.FIXO, 1L)))
                .isInstanceOf(AnuncioInvalidoException.class).hasMessageContaining("incompatíveis");
        assertThat(gateway.salvamentos).isZero();
    }

    @Test void rejeitaAnuncianteInativoAntesDePersistir() {
        gateway.ativo = false;
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.CARRO), true, TipoPreco.FIXO, 1L)))
                .isInstanceOf(AnuncianteInvalidoException.class);
        assertThat(gateway.salvamentos).isZero();
    }

    @Test void rejeitaPrecoIncoerente() {
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.CARRO), true, TipoPreco.SOB_CONSULTA, 1L)))
                .isInstanceOf(AnuncioInvalidoException.class).hasMessageContaining("omitido");
    }

    @Test void rejeitaDadosTecnicosObrigatoriosAusentes() {
        var incompleto = new CriarAnuncioCommand.Carro(1, null, null, null, null, null, null,
                null, null, null, "ABC1D23", false, null, null, null, null);
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, incompleto, true, TipoPreco.FIXO, 1L)))
                .isInstanceOf(AnuncioInvalidoException.class)
                .hasMessageContaining("carroceria");
        assertThat(gateway.salvamentos).isZero();
    }

    @Test void exigeDeclaracaoDeLeilaoESinistro() {
        var base = command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.CARRO), true, TipoPreco.FIXO, 1L);
        var semHistorico = new CriarAnuncioCommand(base.anuncianteId(), base.tipoVeiculo(), base.fabricante(),
                base.modelo(), base.versao(), base.anoFabricacao(), base.anoModelo(), base.cor(),
                base.identificadorPublico(), base.condicao(), base.tipoFreio(), null, null, base.titulo(),
                base.descricao(), base.tipoPreco(), base.precoCentavos(), base.aceitaTroca(),
                base.publicarAgora(), base.endereco(), base.detalhes());

        assertThatThrownBy(() -> useCase.execute(semHistorico))
                .isInstanceOf(AnuncioInvalidoException.class)
                .hasMessageContaining("historicoLeilao");
        assertThat(gateway.salvamentos).isZero();
    }

    @Test void exigePlacaCompletaNosVeiculosEmplacados() {
        var invalido = new CriarAnuncioCommand.Carro(1, "SUV", "AUTOMATICO", "FLEX", null, "2.0", null,
                new java.math.BigDecimal("2.0"), 4, 5, "D23", false, null, null, null, null);
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, invalido, true, TipoPreco.FIXO, 1L)))
                .isInstanceOf(AnuncioInvalidoException.class).hasMessageContaining("7 caracteres");
    }

    @Test void rejeitaQuilometragemPositivaParaVeiculoZeroKm() {
        var base = command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.CARRO), true, TipoPreco.FIXO, 1L);
        var zeroKm = new CriarAnuncioCommand(base.anuncianteId(), base.tipoVeiculo(), base.fabricante(),
                base.modelo(), base.versao(), base.anoFabricacao(), base.anoModelo(), base.cor(),
                base.identificadorPublico(), CondicaoVeiculo.ZERO_KM, base.tipoFreio(),
                base.historicoLeilao(), base.historicoSinistro(), base.titulo(),
                base.descricao(), base.tipoPreco(), base.precoCentavos(), base.aceitaTroca(),
                base.publicarAgora(), base.endereco(), base.detalhes());
        assertThatThrownBy(() -> useCase.execute(zeroKm))
                .isInstanceOf(AnuncioInvalidoException.class)
                .hasMessageContaining("zero km");
        assertThat(gateway.salvamentos).isZero();
    }

    private CriarAnuncioCommand command(TipoVeiculo tipo, CriarAnuncioCommand.DetalhesVeiculo detalhes,
            boolean publicar, TipoPreco tipoPreco, Long preco) {
        return new CriarAnuncioCommand(USUARIO, tipo, "Fabricante", "Modelo", "Versão", 2025, 2026,
                "Preto", null, CondicaoVeiculo.USADO, "DISCO", false, false, "Título", "Descrição", tipoPreco, preco, true, publicar,
                new CriarAnuncioCommand.Endereco("74000000", "Goiânia", "Centro", "Rua 1", "10", null, "GO", 5208707), detalhes);
    }

    private CriarAnuncioCommand.DetalhesVeiculo detalhes(TipoVeiculo tipo) {
        return switch (tipo) {
            case CARRO -> new CriarAnuncioCommand.Carro(1,"SUV","AUTOMATICO","FLEX",null,"2.0",null,new java.math.BigDecimal("2.0"),4,null,"ABC1D23",false,false,false,null,null);
            case MOTO -> new CriarAnuncioCommand.Moto(1,160,"STREET",null,null,"MANUAL","GASOLINA","DEF2E34",true,false,null);
            case CAMINHAO -> new CriarAnuncioCommand.Caminhao(1,"TOCO","BAU","MANUAL","DIESEL",null,null,2,null,null,null,"GHI3F45",false,false,null);
            case CAMINHONETE -> new CriarAnuncioCommand.Caminhonete(1,"DUPLA","PICKUP","AUTOMATICO","DIESEL",null,"2.8",null,new java.math.BigDecimal("2.8"),null,4,"JKL4G56",false,false,false,null,null);
            case BARCO -> new CriarAnuncioCommand.Barco(null,null,null,null,null,null,null,List.of());
            case LINHA_AMARELA -> new CriarAnuncioCommand.LinhaAmarela(null,null,null,null,null,null,null);
        };
    }

    @Test void aceitaMotorLivreEPortasCilindradaELugaresAusentes() {
        var detalhes = new CriarAnuncioCommand.Carro(120000, "Sedã", "Manual", "Gás natural modificado", null,
                "4.1 Turbo", null, null, null, null, "ABC1234", false, false, false, null, null);
        var resultado = useCase.execute(command(TipoVeiculo.CARRO, detalhes, false, TipoPreco.FIXO, 1L));
        assertThat(((CriarAnuncioCommand.Carro) resultado.dados().detalhes()).motorizacao()).isEqualTo("4.1 Turbo");
    }

    @Test void exigeRespostasExplicitasSobreIpvaEDono() {
        for (boolean omitirIpva : List.of(true, false)) {
            var detalhes = new CriarAnuncioCommand.Carro(10, "Sedã", "Manual", "Flex", null,
                    "4.1", null, null, null, null, "ABC1234", false,
                    omitirIpva ? false : null, omitirIpva ? null : false, null, null);
            assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, detalhes, false, TipoPreco.FIXO, 1L)))
                    .isInstanceOf(AnuncioInvalidoException.class).hasMessageContaining(omitirIpva ? "ipvaPago" : "unicoDono");
        }
    }

    @Test void rejeitaFormatoDeMotorInvalido() {
        var detalhes = new CriarAnuncioCommand.Carro(10, "Sedã", "Manual", "Flex", null,
                "qualquer texto", null, null, null, null, "ABC1234", false, false, false, null, null);
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, detalhes, false, TipoPreco.FIXO, 1L)))
                .isInstanceOf(AnuncioInvalidoException.class).hasMessageContaining("Motorização");
    }

    private static final class Gateway implements CriarAnuncioGateway {
        boolean ativo = true; int salvamentos;
        public boolean usuarioAtivo(UUID id) { return ativo; }
        public void salvar(UUID anuncioId, UUID veiculoId, UUID enderecoId, CriarAnuncioCommand dados,
                StatusAnuncio status, Instant agora) { salvamentos++; }
    }
}
