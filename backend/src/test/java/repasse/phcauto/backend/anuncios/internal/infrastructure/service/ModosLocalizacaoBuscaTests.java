package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import repasse.phcauto.backend.anuncios.ModoLocalizacaoBusca;
import repasse.phcauto.backend.anuncios.internal.core.domain.*;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnunciosUseCase;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.BuscarAnunciosRequest;
import repasse.phcauto.backend.localizacao.*;

class ModosLocalizacaoBuscaTests {
    private final AtomicReference<BuscarAnunciosFiltro> recebido = new AtomicReference<>();
    private final BuscarAnunciosUseCase busca = filtro -> {
        recebido.set(filtro);
        return new PaginaAnuncios(List.of(), 0, filtro.pagina(), filtro.tamanho(),
                filtro.buscaPorProximidade() ? 200 : null, false);
    };
    private final LocalizacaoFacade localizacao = new LocalizacaoFacade() {
        public MunicipioLocalizacao buscarMunicipio(String cidade, String uf) {
            return new MunicipioLocalizacao(5212501, "Luziânia", "GO", -16.252, -47.95);
        }
        public MunicipioLocalizacao buscarMunicipio(int codigoIbge) {
            return new MunicipioLocalizacao(codigoIbge, "São Paulo", "SP", -23.55, -46.63);
        }
    };

    @Test void cidadeEhConvertidaEmCoordenadas() {
        facade().buscar(request(ModoLocalizacaoBusca.CIDADE, "Luziânia", "GO", null, null), null);
        assertThat(recebido.get().latitude()).isEqualTo(-16.252);
        assertThat(recebido.get().longitude()).isEqualTo(-47.95);
    }

    @Test void dispositivoUsaCoordenadasSemPersistirEndereco() {
        facade().buscar(request(ModoLocalizacaoBusca.DISPOSITIVO, null, null, -19.9, -43.9), null);
        assertThat(recebido.get().latitude()).isEqualTo(-19.9);
        assertThat(recebido.get().uf()).isNull();
    }

    @Test void ufFiltraEstadoSemAplicarRaio() {
        facade().buscar(request(ModoLocalizacaoBusca.UF, null, "MG", null, null), null);
        assertThat(recebido.get().uf()).isEqualTo("MG");
        assertThat(recebido.get().buscaPorProximidade()).isFalse();
    }

    @Test void ausenciaDeLocalizacaoBuscaBrasilInteiro() {
        facade().buscar(request(null, null, null, null, null), null);
        assertThat(recebido.get().uf()).isNull();
        assertThat(recebido.get().buscaPorProximidade()).isFalse();
    }

    private DefaultAnunciosFacade facade() {
        PlatformTransactionManager tx = Mockito.mock(PlatformTransactionManager.class);
        Mockito.when(tx.getTransaction(Mockito.any())).thenReturn(Mockito.mock(TransactionStatus.class));
        return new DefaultAnunciosFacade(busca, command -> null, (id, patch) -> null, id -> {},
                localizacao, id -> new Coordenadas(-23, -46), tx);
    }

    private BuscarAnunciosRequest request(ModoLocalizacaoBusca modo, String cidade, String uf,
            Double latitude, Double longitude) {
        return new BuscarAnunciosRequest(modo, null, cidade, uf, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                latitude, longitude, 0, 52);
    }
}
