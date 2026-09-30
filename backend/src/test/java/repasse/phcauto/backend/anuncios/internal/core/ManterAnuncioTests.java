package repasse.phcauto.backend.anuncios.internal.core;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.AnuncioAtualizado;
import repasse.phcauto.backend.anuncios.AnuncioExcluido;
import repasse.phcauto.backend.domain.model.catalogo.*;

class ManterAnuncioTests {
    private final Instant agora=Instant.parse("2026-09-28T12:00:00Z");
    private final Clock clock=Clock.fixed(agora, ZoneOffset.UTC);
    private final List<Object> eventos=new ArrayList<>();

    @Test void atualizaEPublicaEvento() {
        var esperado=resultado();
        AtualizarAnuncioGateway gateway=(id,c,instante)->{ assertThat(id).isEqualTo(esperado.anuncioId()); assertThat(instante).isEqualTo(agora); return esperado; };
        var useCase=new AtualizarAnuncio(gateway,eventos::add,clock);
        var resposta=useCase.execute(esperado.anuncioId(),new AtualizarAnuncioCommand(null,null,null,null,null,null,null,null,null,"Novo título",null,null,null,null,null,null,null));
        assertThat(resposta).isSameAs(esperado);
        assertThat(eventos).singleElement().isInstanceOf(AnuncioAtualizado.class);
    }

    @Test void rejeitaAnoInvalidoAntesDoGateway() {
        AtualizarAnuncioGateway gateway=(id,c,i)->{throw new AssertionError("não deveria persistir");};
        var useCase=new AtualizarAnuncio(gateway,eventos::add,clock);
        var command=new AtualizarAnuncioCommand(null,null,null,1800,null,null,null,null,null,null,null,null,null,null,null,null,null);
        assertThatThrownBy(()->useCase.execute(UUID.randomUUID(),command)).isInstanceOf(AnuncioInvalidoException.class);
    }

    @Test void excluiEPublicaEventoComIdentificadores() {
        UUID anuncio=UUID.randomUUID(),veiculo=UUID.randomUUID();
        var useCase=new ExcluirAnuncio(id->{assertThat(id).isEqualTo(anuncio);return veiculo;},eventos::add,clock);
        useCase.execute(anuncio);
        assertThat(eventos).singleElement().isEqualTo(new AnuncioExcluido(((AnuncioExcluido)eventos.get(0)).eventoId(),anuncio,veiculo,agora));
    }

    private AnuncioCriadoResultado resultado(){
        UUID a=UUID.randomUUID(),v=UUID.randomUUID(),u=UUID.randomUUID();
        var c=new CriarAnuncioCommand(u,TipoVeiculo.CARRO,"F","M",null,2025,2026,null,null,CondicaoVeiculo.USADO,"DISCO","T",null,TipoPreco.FIXO,1L,false,true,
                new CriarAnuncioCommand.Endereco("74000000","Goiânia","Centro","Rua","1",null,"GO"),
                new CriarAnuncioCommand.Carro(1,null,null,null,null,null,null,null,null,null,"ABC1D23",false,null,null,null,null));
        return new AnuncioCriadoResultado(a,v,c,StatusAnuncio.PUBLICADO,agora,agora);
    }
}
