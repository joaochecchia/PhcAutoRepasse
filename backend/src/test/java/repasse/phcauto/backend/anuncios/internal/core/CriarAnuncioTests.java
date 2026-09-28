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
                null, null, null, null, null, null, null, null);
        assertThatThrownBy(() -> useCase.execute(command(TipoVeiculo.CARRO, incompleto, true, TipoPreco.FIXO, 1L)))
                .isInstanceOf(AnuncioInvalidoException.class)
                .hasMessageContaining("carroceria");
        assertThat(gateway.salvamentos).isZero();
    }

    @Test void rejeitaQuilometragemPositivaParaVeiculoZeroKm() {
        var base = command(TipoVeiculo.CARRO, detalhes(TipoVeiculo.CARRO), true, TipoPreco.FIXO, 1L);
        var zeroKm = new CriarAnuncioCommand(base.anuncianteId(), base.tipoVeiculo(), base.fabricante(),
                base.modelo(), base.versao(), base.anoFabricacao(), base.anoModelo(), base.cor(),
                base.identificadorPublico(), CondicaoVeiculo.ZERO_KM, base.tipoFreio(), base.titulo(),
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
                "Preto", null, CondicaoVeiculo.USADO, "DISCO", "Título", "Descrição", tipoPreco, preco, true, publicar,
                new CriarAnuncioCommand.Endereco("74000000", "Goiânia", "Centro", "Rua 1", "10", null, "GO"), detalhes);
    }

    private CriarAnuncioCommand.DetalhesVeiculo detalhes(TipoVeiculo tipo) {
        return switch (tipo) {
            case CARRO -> new CriarAnuncioCommand.Carro(1,"SUV","AUTOMATICO","FLEX",null,"2.0",null,new java.math.BigDecimal("2.0"),4,null,null,null,null,null,null);
            case MOTO -> new CriarAnuncioCommand.Moto(1,160,"STREET",null,null,"MANUAL","GASOLINA",null,null,null);
            case CAMINHAO -> new CriarAnuncioCommand.Caminhao(1,"TOCO","BAU","MANUAL","DIESEL",null,null,2,null,null,null,null,null,null);
            case CAMINHONETE -> new CriarAnuncioCommand.Caminhonete(1,"DUPLA","PICKUP","AUTOMATICO","DIESEL",null,"2.8",null,new java.math.BigDecimal("2.8"),null,4,null,null,null,null,null);
            case BARCO -> new CriarAnuncioCommand.Barco(null,null,null,null,null,null,null,List.of());
            case LINHA_AMARELA -> new CriarAnuncioCommand.LinhaAmarela(null,null,null,null,null,null,null);
        };
    }

    private static final class Gateway implements CriarAnuncioGateway {
        boolean ativo = true; int salvamentos;
        public boolean usuarioAtivo(UUID id) { return ativo; }
        public void salvar(UUID anuncioId, UUID veiculoId, UUID enderecoId, CriarAnuncioCommand dados,
                StatusAnuncio status, Instant agora) { salvamentos++; }
    }
}
