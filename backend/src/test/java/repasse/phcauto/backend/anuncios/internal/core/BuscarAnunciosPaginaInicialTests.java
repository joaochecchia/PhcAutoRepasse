package repasse.phcauto.backend.anuncios.internal.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosPaginaInicialGateway;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnunciosPaginaInicial;

class BuscarAnunciosPaginaInicialTests {
    @Test
    void limitaAQuantidadeEDelegaAoGateway() {
        var gateway = new GatewayProbe();

        new BuscarAnunciosPaginaInicial(gateway).execute(20);

        org.assertj.core.api.Assertions.assertThat(gateway.limite).isEqualTo(20);
    }

    @Test
    void rejeitaLimiteForaDoContratoDaPaginaInicial() {
        var useCase = new BuscarAnunciosPaginaInicial(new GatewayProbe());

        assertThatThrownBy(() -> useCase.execute(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.execute(21)).isInstanceOf(IllegalArgumentException.class);
    }

    private static final class GatewayProbe implements BuscarAnunciosPaginaInicialGateway {
        private int limite;
        @Override public List<repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioResumo> buscar(int limite) {
            this.limite = limite;
            return List.of();
        }
    }
}
