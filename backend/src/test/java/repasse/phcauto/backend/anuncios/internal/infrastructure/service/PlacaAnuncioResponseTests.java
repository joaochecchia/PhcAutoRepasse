package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.*;
import repasse.phcauto.backend.domain.model.catalogo.*;

import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioCriadoResultado;
import repasse.phcauto.backend.anuncios.internal.core.usecase.CriarAnuncioUseCase;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.CriarAnuncioRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.EnderecoAnuncioRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.AnuncioResponse;
import repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
class PlacaAnuncioResponseTests {
    @Test void retornaSomenteFinalQuandoAnuncianteOcultaPlaca() {
        assertThat(criar(false).detalhes()).isInstanceOfSatisfying(AnuncioResponse.CarroResponse.class,
                carro -> assertThat(carro.placa()).isEqualTo("3"));
    }

    @Test void retornaPlacaNormalizadaQuandoAnuncianteAutoriza() {
        assertThat(criar(true).detalhes()).isInstanceOfSatisfying(AnuncioResponse.CarroResponse.class, carro -> {
            assertThat(carro.placa()).isEqualTo("ABC1D23");
            assertThat(carro.placaCompletaVisivel()).isTrue();
        });
    }

    private AnuncioResponse criar(boolean exibir) {
        CriarAnuncioUseCase criar = command -> new AnuncioCriadoResultado(UUID.randomUUID(), UUID.randomUUID(),
                command, StatusAnuncio.PUBLICADO, Instant.EPOCH, Instant.EPOCH);
        var transactionManager = org.mockito.Mockito.mock(org.springframework.transaction.PlatformTransactionManager.class);
        org.mockito.Mockito.when(transactionManager.getTransaction(org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.mockito.Mockito.mock(org.springframework.transaction.TransactionStatus.class));
        repasse.phcauto.backend.localizacao.LocalizacaoFacade localizacao =
                new repasse.phcauto.backend.localizacao.LocalizacaoFacade() {
                    public repasse.phcauto.backend.localizacao.MunicipioLocalizacao buscarMunicipio(String cidade, String uf) {
                        return new repasse.phcauto.backend.localizacao.MunicipioLocalizacao(
                                5208707, "Goiânia", "GO", -16.6869, -49.2648);
                    }
                    public repasse.phcauto.backend.localizacao.MunicipioLocalizacao buscarMunicipio(int codigoIbge) {
                        return buscarMunicipio("Goiânia", "GO");
                    }
                };
        var facade = new DefaultAnunciosFacade(f -> null, criar, (id, patch) -> null, id -> { },
                localizacao, null, transactionManager);
        var endereco = new EnderecoAnuncioRequest("74000000", "Goiânia", "Centro", "Rua", "1", null, "GO");
        var carro = new CriarAnuncioRequest.CarroRequest(10, "SUV", "AUTOMATICO", "FLEX", null,
                "2.0", null, new BigDecimal("2.0"), 4, 5, "abc1d23", exibir,
                true, true, true, false);
        return facade.criar(new CriarAnuncioRequest(UUID.randomUUID(), TipoVeiculo.CARRO, "Fabricante", "Modelo",
                null, 2025, 2026, "Preto", null, CondicaoVeiculo.USADO, "ABS", "Título", null,
                TipoPreco.FIXO, 100L, false, true, endereco, carro, null, null, null, null, null));
    }
}
