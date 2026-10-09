package repasse.phcauto.backend.anuncios;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.internal.core.domain.CatalogoReferenciaVeicular;

class CatalogoReferenciaVeicularTest {
    @Test
    void normalizaAliasDeFabricanteParaValorCanonico() {
        assertThat(CatalogoReferenciaVeicular.normalizarFabricante("vw"))
                .contains("Volkswagen");
        assertThat(CatalogoReferenciaVeicular.normalizarFabricante("MERCEDES"))
                .contains("Mercedes-Benz");
    }

    @Test
    void aceitaModeloSemDiferenciarMaiusculasOuAcentos() {
        assertThat(CatalogoReferenciaVeicular.normalizarModelo("VW", "gOl"))
                .contains("Gol");
        assertThat(CatalogoReferenciaVeicular.normalizarOpcao(
                CatalogoReferenciaVeicular.CAMBIOS, "automatico"))
                .contains("Automático");
    }

    @Test
    void rejeitaModeloQueNaoPertenceAoFabricante() {
        assertThat(CatalogoReferenciaVeicular.normalizarModelo("Volkswagen", "Onix"))
                .isEmpty();
    }

    @Test
    void incluiNovasMarcasEModelosNasSugestoes() {
        assertThat(CatalogoReferenciaVeicular.normalizarFabricante("gac motors"))
                .contains("GAC");
        assertThat(CatalogoReferenciaVeicular.normalizarModelo("GAC", "gs3"))
                .contains("GS3");
        assertThat(CatalogoReferenciaVeicular.modelosPorFabricante())
                .containsKeys("Geely", "Omoda", "Jaecoo", "Zeekr");
    }

    @Test
    void normalizaFabricantesEModelosDeMotos() {
        assertThat(CatalogoReferenciaVeicular.normalizarFabricanteMoto("harley"))
                .contains("Harley-Davidson");
        assertThat(CatalogoReferenciaVeicular.normalizarModeloMoto("Honda Motos", "cg 160"))
                .contains("CG 160");
        assertThat(CatalogoReferenciaVeicular.modelosMotosPorFabricante())
                .containsKeys("Yamaha", "Royal Enfield", "Bajaj");
        assertThat(CatalogoReferenciaVeicular.CAMBIOS_MOTO)
                .contains("Manual sequencial", "Automático DCT");
        assertThat(CatalogoReferenciaVeicular.TIPOS_FREIO_MOTO)
                .contains("CBS", "Disco dianteiro e tambor traseiro")
                .doesNotContain("Disco nas quatro rodas");
    }
}
