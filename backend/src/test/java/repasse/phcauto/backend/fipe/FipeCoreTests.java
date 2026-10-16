package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.gateway.*;
import repasse.phcauto.backend.fipe.internal.core.usecase.*;
class FipeCoreTests {
    final LocalDate mes=LocalDate.of(2026,10,1);
    final Clock clock=Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"),ZoneOffset.UTC);
    DadosReferencia dados(){return new DadosReferencia("014001-4","CARRO","Honda","Civic EXL 2.0 Flex",2022,"Gasolina",mes,7500000);}
    ConsultaExterna consulta(){return new ConsultaExterna("cars","25","123","2022-1",338,mes);}
    ReferenciaFipe ref(){return new ReferenciaFipe(UUID.randomUUID(),"014001-4","CARRO","Honda","Civic EXL 2.0 Flex",2022,false,"Gasolina",mes,7500000,"PARALLELUM");}
    @Test void usaBancoAntesDoFornecedor(){
        var banco=mock(CatalogoFipeGateway.class);var sobDemanda=mock(ReferenciaSobDemandaFipeGateway.class);var r=ref();var c=consulta();
        when(banco.buscarConsulta(c.chave(),mes)).thenReturn(Optional.of(r));
        assertThat(new ResolverReferenciaFipe(banco,sobDemanda).execute(c)).isEqualTo(r);verifyNoInteractions(sobDemanda);
    }
    @Test void consultaAusenteTentaResolverSobDemanda() {
        var banco=mock(CatalogoFipeGateway.class);var sobDemanda=mock(ReferenciaSobDemandaFipeGateway.class);var c=consulta();
        when(banco.buscarConsulta(c.chave(),mes)).thenReturn(Optional.empty());
        when(sobDemanda.buscar(c)).thenReturn(Optional.empty());
        assertThatThrownBy(()->new ResolverReferenciaFipe(banco,sobDemanda).execute(c)).isInstanceOfSatisfying(FipeException.class,
            e->assertThat(e.motivo()).isEqualTo(FipeException.Motivo.NAO_ENCONTRADA));
        verify(sobDemanda).buscar(c);
        verify(banco,never()).salvarConsulta(any(),any());
    }
    @Test void validaMesELimitesDaImportacaoAntesDeGravar(){
        var gateway=mock(ImportarLoteFipeGateway.class);var useCase=new ImportarLoteFipe(gateway,clock);var id=UUID.randomUUID();
        assertThatThrownBy(()->useCase.execute(id,"ARQUIVO",mes.minusMonths(1),List.of(dados()))).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->useCase.execute(id,"ARQUIVO",mes,List.of(dados(),dados()))).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->useCase.execute(id,"ARQUIVO",mes,Collections.nCopies(101,dados()))).isInstanceOf(FipeException.class);
        verifyNoInteractions(gateway);
        useCase.execute(id,"ARQUIVO",mes,List.of(dados()));verify(gateway).salvar(id,"ARQUIVO",List.of(dados()));
    }
    @Test void validaConsultaEValores(){
        assertThatThrownBy(()->new PesquisaFipe("CARRO",null,null,null,null,0,51)).isInstanceOf(FipeException.class);
        assertThat(new PesquisaFipe("CAMINHONETE",null,null,null,null,0,50).tipo()).isEqualTo("CARRO");
        assertThatThrownBy(()->new ConsultaExterna("cars","../../x","1","2022-1",1,mes)).isInstanceOf(FipeException.class);
        assertThatThrownBy(()->new DadosReferencia("014001-4","CARRO","Honda","Civic",2022,"Gasolina",mes,0)).isInstanceOf(FipeException.class);
        assertThat(new DadosReferencia("014001-4","CARRO","Honda","Civic",32000,"Gasolina",mes,1).zeroKm()).isTrue();
    }
    @Test void sugereSomenteVersaoAnoECombustivelCompativeis(){
        var gateway=mock(OpcoesFipeGateway.class);
        when(gateway.opcoes(any())).thenAnswer(invocation->{
            ConsultaOpcoesFipe q=invocation.getArgument(0);
            if(q.tipo()==null)return List.of(new OpcaoFipe("338","outubro de 2026",mes));
            if(q.marcaId()==null)return List.of(new OpcaoFipe("25","Honda"));
            if(q.modeloId()==null)return List.of(new OpcaoFipe("123","Civic EXL 2.0 Flex"),new OpcaoFipe("124","Civic Touring 1.5 Turbo"));
            return List.of(new OpcaoFipe("2022-1","2022 Gasolina"),new OpcaoFipe("2021-1","2021 Gasolina"));
        });
        var comando=new repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe(
                "CARRO","Honda","Civic","EXL 2.0 Flex",2022,"USADO","FLEX");
        var resultado=new repasse.phcauto.backend.fipe.internal.core.usecase.BuscarSugestoesFipe(gateway).execute(comando);
        assertThat(resultado).singleElement().satisfies(s->{assertThat(s.modeloId()).isEqualTo("123");assertThat(s.anoId()).isEqualTo("2022-1");});
    }
    @Test void naoAdivinhaVersaoAproximada(){
        var gateway=mock(OpcoesFipeGateway.class);
        when(gateway.opcoes(any())).thenAnswer(invocation->{
            ConsultaOpcoesFipe q=invocation.getArgument(0);
            if(q.tipo()==null)return List.of(new OpcaoFipe("338","outubro de 2026",mes));
            if(q.marcaId()==null)return List.of(new OpcaoFipe("25","Honda"));
            return List.of(new OpcaoFipe("123","Civic EXL 2.0 Flex"));
        });
        var comando=new repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe(
                "CARRO","Honda","Civic","Touring",2022,"USADO","FLEX");
        assertThat(new repasse.phcauto.backend.fipe.internal.core.usecase.BuscarSugestoesFipe(gateway).execute(comando)).isEmpty();
    }
}
