package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.usecase.*;
/** Fluxo marca → ano → modelo/versão: anos sem combustível e modelos somando os combustíveis do ano. */
class FipeCatalogoPorAnoTests {
    /** Gateway em memória: escopo → opções; escopos em {@code pendentes} voltam pendentes. */
    static final class Catalogo implements OpcoesFipeGateway {
        final Map<String,List<OpcaoFipe>> niveis=new HashMap<>();
        final Set<String> pendentes=new HashSet<>();
        final List<String> carregados=new ArrayList<>();
        public List<OpcaoFipe> opcoes(ConsultaOpcoesFipe c){return niveis.getOrDefault(EscopoCatalogoFipe.chave(c),List.of());}
        @Override public List<CargaOpcoesFipe> carregar(List<ConsultaOpcoesFipe> consultas){
            return consultas.stream().map(c->{String e=EscopoCatalogoFipe.chave(c);carregados.add(e);
                return new CargaOpcoesFipe(opcoes(c),pendentes.contains(e));}).toList();
        }
    }
    final Catalogo catalogo=new Catalogo();
    {
        catalogo.niveis.put("338:cars:21:anos",List.of(new OpcaoFipe("32000-5","32000 Flex"),new OpcaoFipe("2020-5","2020 Flex"),
            new OpcaoFipe("2020-1","2020 Gasolina"),new OpcaoFipe("2019-5","2019 Flex")));
        catalogo.niveis.put("338:cars:21:ano:2020-5",List.of(new OpcaoFipe("10","Argo Drive 1.0"),new OpcaoFipe("20","Strada Freedom 1.3")));
        catalogo.niveis.put("338:cars:21:ano:2020-1",List.of(new OpcaoFipe("21","Strada Freedom 1.3"),new OpcaoFipe("30","Toro Volcano 2.4")));
    }
    @Test void anosDaMarcaSemRepetirCombustivelEComZeroKmPrimeiro(){
        var pagina=new ConsultarAnosMarcaFipe(catalogo).execute(new ConsultaOpcoesFipe("cars","21",null,338,"",0,50,null,true));
        assertThat(pagina.opcoes()).extracting(OpcaoFipe::codigo).containsExactly("32000","2020","2019");
        assertThat(pagina.opcoes()).extracting(OpcaoFipe::nome).containsExactly("Zero km","2020","2019");
        assertThat(pagina.catalogoPendente()).isFalse();
    }
    @Test void modelosDoAnoSomamCombustiveisEDiferenciamSoOsRepetidos(){
        var pagina=new ConsultarModelosAnoFipe(catalogo).execute(new ConsultaModelosAnoFipe("cars","21",2020,338,"",0,50));
        assertThat(pagina.opcoes()).extracting(OpcaoFipe::nome)
            .containsExactly("Argo Drive 1.0","Strada Freedom 1.3 — Flex","Strada Freedom 1.3 — Gasolina","Toro Volcano 2.4");
        assertThat(pagina.opcoes()).extracting(OpcaoFipe::anoId).containsExactly("2020-5","2020-5","2020-1","2020-1");
        assertThat(catalogo.carregados).containsExactly("338:cars:21:anos","338:cars:21:ano:2020-5","338:cars:21:ano:2020-1");
    }
    @Test void filtraPorTermoEPagina(){
        var pagina=new ConsultarModelosAnoFipe(catalogo).execute(new ConsultaModelosAnoFipe("cars","21",2020,338,"strada",0,1));
        assertThat(pagina.opcoes()).extracting(OpcaoFipe::nome).containsExactly("Strada Freedom 1.3 — Flex");
        assertThat(pagina.temProximaPagina()).isTrue();
    }
    @Test void combustivelPendenteDevolveOsCarregadosEMarcaPendente(){
        catalogo.pendentes.add("338:cars:21:ano:2020-1");
        catalogo.niveis.remove("338:cars:21:ano:2020-1");
        var pagina=new ConsultarModelosAnoFipe(catalogo).execute(new ConsultaModelosAnoFipe("cars","21",2020,338,"",0,50));
        assertThat(pagina.opcoes()).extracting(OpcaoFipe::nome).containsExactly("Argo Drive 1.0","Strada Freedom 1.3");
        assertThat(pagina.catalogoPendente()).isTrue();
    }
    @Test void anoSemModelosNaoConsultaNiveisDeModelo(){
        var pagina=new ConsultarModelosAnoFipe(catalogo).execute(new ConsultaModelosAnoFipe("cars","21",1990,338,"",0,50));
        assertThat(pagina.opcoes()).isEmpty();
        assertThat(pagina.catalogoPendente()).isFalse();
        assertThat(catalogo.carregados).containsExactly("338:cars:21:anos");
    }
    @Test void escoposPorAnoPendemDoNivelAnterior(){
        var anos=ConsultaOpcoesFipe.anosDaMarca("cars","21",338);
        var modelos=ConsultaOpcoesFipe.modelosDoAno("cars","21","2020-5",338);
        assertThat(EscopoCatalogoFipe.chave(anos)).isEqualTo("338:cars:21:anos");
        assertThat(EscopoCatalogoFipe.pai(anos)).isEqualTo("338:cars");
        assertThat(EscopoCatalogoFipe.codigoPai(anos)).isEqualTo("21");
        assertThat(EscopoCatalogoFipe.chave(modelos)).isEqualTo("338:cars:21:ano:2020-5");
        assertThat(EscopoCatalogoFipe.pai(modelos)).isEqualTo("338:cars:21:anos");
        assertThat(EscopoCatalogoFipe.codigoPai(modelos)).isEqualTo("2020-5");
        assertThat(EscopoCatalogoFipe.chave(new ConsultaOpcoesFipe("cars","21","55",338,"",0,50))).isEqualTo("338:cars:21:55");
    }
    @Test void rejeitaCombinacoesInvalidas(){
        assertThatThrownBy(()->new ConsultaOpcoesFipe("cars",null,null,338,"",0,50,null,true)).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->new ConsultaOpcoesFipe("cars","21","55",338,"",0,50,"2020-5",false)).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->new ConsultaOpcoesFipe("cars","21",null,338,"",0,50,"2020",false)).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->new ConsultaModelosAnoFipe("cars","21",1500,338,"",0,50)).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->new ConsultaModelosAnoFipe("cars",null,2020,338,"",0,50)).isInstanceOf(FipeException.class);
    }
    @Test void filaPreservaONivelPorAno(){
        var comando=SincronizarFipeCommand.opcoes(ConsultaOpcoesFipe.modelosDoAno("cars","21","2020-5",338));
        assertThat(comando.chave()).isEqualTo("338:cars:21:ano:2020-5");
        assertThat(comando.anoId()).isNull();
        assertThat(SincronizarFipeCommand.opcoes(ConsultaOpcoesFipe.anosDaMarca("cars","21",338)).chave()).isEqualTo("338:cars:21:anos");
    }
}
