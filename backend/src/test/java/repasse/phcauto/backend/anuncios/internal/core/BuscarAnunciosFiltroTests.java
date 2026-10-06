package repasse.phcauto.backend.anuncios.internal.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.internal.core.domain.BuscarAnunciosFiltro;

class BuscarAnunciosFiltroTests {

    @Test
    void aceitaPaginaComNoMaximoCinquentaEDoisItens() {
        var filtro = filtro(-16.6869, -49.2648, 52);
        assertThat(filtro.tamanho()).isEqualTo(52);
        assertThat(filtro.buscaPorProximidade()).isTrue();
    }

    @Test
    void rejeitaPaginaAcimaDoLimite() {
        assertThatThrownBy(() -> filtro(-16.6869, -49.2648, 53))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("52");
    }

    @Test
    void rejeitaCoordenadasForaDosLimitesGeograficos() {
        assertThatThrownBy(() -> filtro(-91.0, -49.2648, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coordenadas geográficas");
    }

    @Test
    void rejeitaCoordenadasIncompletas() {
        assertThatThrownBy(() -> filtro(-16.6869, null, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude e longitude");
    }

    private static BuscarAnunciosFiltro filtro(Double latitude, Double longitude, int tamanho) {
        return new BuscarAnunciosFiltro(null, "Goiânia", "GO", null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, latitude, longitude, 0, tamanho);
    }
}
