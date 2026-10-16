package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import repasse.phcauto.backend.anuncios.internal.core.domain.*;
import repasse.phcauto.backend.anuncios.internal.core.usecase.*;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.domain.model.catalogo.*;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.FipeAnuncioResponse;
class FipeVeiculoTests {
    final UUID id=UUID.randomUUID();
    final FipeVeiculo snapshot=new FipeVeiculo(id,"014001-4",7500000,LocalDate.of(2026,10,1),"Civic EXL Flex","TESTE");
    CriarAnuncioCommand veiculo(TipoVeiculo tipo,String versao,Integer ano,CondicaoVeiculo condicao){
        CriarAnuncioCommand.DetalhesVeiculo d=switch(tipo){
            case CARRO->new CriarAnuncioCommand.Carro(100,"HATCH","MANUAL","FLEX",null,"2.0",null,null,4,5,"ABC1D23",false,true,true,true,false);
            case MOTO->new CriarAnuncioCommand.Moto(100,160,"STREET",null,null,"MANUAL","FLEX","ABC1D23",false,true,true);
            case CAMINHAO->new CriarAnuncioCommand.Caminhao(100,"TOCO","BAU","MANUAL","FLEX",null,null,2,null,null,null,"ABC1D23",false,true,true);
            case CAMINHONETE->new CriarAnuncioCommand.Caminhonete(100,"DUPLA","PICKUP","MANUAL","FLEX",null,"2.0",null,null,null,4,"ABC1D23",false,true,true,true,false);
            case BARCO->new CriarAnuncioCommand.Barco(null,null,null,null,null,null,null,List.of());
            case LINHA_AMARELA->new CriarAnuncioCommand.LinhaAmarela(null,null,null,null,null,null,null);
        };
        return new CriarAnuncioCommand(UUID.randomUUID(),tipo,"Honda","Civic",versao,2022,ano,"Preto",null,condicao,null,false,false,
            "Teste",null,TipoPreco.FIXO,7290000L,false,true,null,d,id,null);
    }
    ReferenciaFipeVeiculo referencia(String tipo,int ano,boolean zero){return new ReferenciaFipeVeiculo(snapshot,tipo,"Honda","Civic EXL Flex",ano,zero,"Gasolina");}
    @ParameterizedTest @EnumSource(value=TipoVeiculo.class,names={"CARRO","MOTO","CAMINHAO","CAMINHONETE"})
    void vinculaSomenteTiposCobertos(TipoVeiculo tipo){
        var useCase=new VincularFipeVeiculo(x->referencia(tipo==TipoVeiculo.CAMINHONETE?"CARRO":tipo.name(),2022,false));
        assertThat(useCase.execute(veiculo(tipo,"Civic EXL Flex",2022,CondicaoVeiculo.USADO),id).fipe()).isEqualTo(snapshot);
    }
    @ParameterizedTest @EnumSource(value=TipoVeiculo.class,names={"BARCO","LINHA_AMARELA"})
    void rejeitaTiposSemCobertura(TipoVeiculo tipo){
        var useCase=new VincularFipeVeiculo(x->{throw new AssertionError("Não consultar");});
        assertThatThrownBy(()->useCase.execute(veiculo(tipo,"Civic EXL Flex",2022,CondicaoVeiculo.USADO),id)).isInstanceOf(AnuncioInvalidoException.class);
    }
    @Test void rejeitaVersaoAmbiguaEAnoErrado(){
        var u=new VincularFipeVeiculo(x->referencia("CARRO",2022,false));
        assertThatThrownBy(()->u.execute(veiculo(TipoVeiculo.CARRO,"Completo",2022,CondicaoVeiculo.USADO),id)).isInstanceOf(AnuncioInvalidoException.class);
        assertThatThrownBy(()->u.execute(veiculo(TipoVeiculo.CARRO,"Civic EXL Flex",2021,CondicaoVeiculo.USADO),id)).isInstanceOf(AnuncioInvalidoException.class);
    }
    @Test void zeroKmTemReferenciaPropria(){
        var u=new VincularFipeVeiculo(x->referencia("CARRO",32000,true));
        assertThat(u.execute(veiculo(TipoVeiculo.CARRO,"Civic EXL Flex",2026,CondicaoVeiculo.ZERO_KM),id).fipe()).isNotNull();
        assertThatThrownBy(()->u.execute(veiculo(TipoVeiculo.CARRO,"Civic EXL Flex",2026,CondicaoVeiculo.USADO),id)).isInstanceOf(AnuncioInvalidoException.class);
    }
    AtualizarAnuncioCommand patch(UUID id,boolean remover){return new AtualizarAnuncioCommand(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,id,remover);}
    @Test void patchPreservaSnapshotOuLimpaQuandoIdentidadeMuda(){
        var u=new ReconciliarFipeVeiculo(new VincularFipeVeiculo(x->referencia("CARRO",2022,false)));
        var original=veiculo(TipoVeiculo.CARRO,"Civic EXL Flex",2022,CondicaoVeiculo.USADO);
        assertThat(u.execute(original,original,patch(null,false),snapshot).fipe()).isEqualTo(snapshot);
        var outro=veiculo(TipoVeiculo.CARRO,"Outra versão",2022,CondicaoVeiculo.USADO);
        assertThat(u.execute(original,outro,patch(null,false),snapshot).fipe()).isNull();
        assertThat(u.execute(original,original,patch(null,true),snapshot).fipe()).isNull();
        assertThatThrownBy(()->u.execute(original,original,patch(id,true),snapshot)).isInstanceOf(AnuncioInvalidoException.class);
    }
    @Test void calculaDiferencaNoBackendSemInventarPrecoSobConsulta(){
        var resposta=FipeAnuncioResponse.de(snapshot,7290000L);
        assertThat(resposta.diferencaCentavos()).isEqualTo(-210000);
        assertThat(resposta.diferencaPercentual()).isEqualByComparingTo("-2.80");
        assertThat(FipeAnuncioResponse.de(snapshot,null).diferencaPercentual()).isNull();
        assertThat(FipeAnuncioResponse.de(null,7290000L)).isNull();
    }
}
