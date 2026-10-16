package repasse.phcauto.backend.fipe.internal.infrastructure.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaExterna;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.JpaOpcoesFipeGateway;

class ResolucaoSobDemandaFipeTests {
    private final LocalDate mes=LocalDate.of(2026,10,1);
    private final ConsultaExterna consulta=new ConsultaExterna("cars","56","10909","2026-5",338,mes);
    private final JpaOpcoesFipeGateway opcoes=mock(JpaOpcoesFipeGateway.class);
    private final ParallelumClient fornecedor=mock(ParallelumClient.class);
    private final CatalogoFipeGateway catalogo=mock(CatalogoFipeGateway.class);

    @Test void semAutorizacaoNaoConsultaFornecedor() {
        assertThat(new ResolucaoSobDemandaFipe(opcoes,fornecedor,catalogo,false).buscar(consulta)).isEmpty();
        verifyNoInteractions(opcoes,fornecedor,catalogo);
    }

    @Test void consultaApenasHierarquiaLocalValidaEPersistePrecoRecebido() {
        var dados=new DadosReferencia("002216-0","CARRO","Toyota","Corolla Cross XRX 2.0",2026,"FLEX",mes,18472100);
        var referencia=new ReferenciaFipe(UUID.randomUUID(),dados.codigoFipe(),dados.tipo(),dados.marca(),
            dados.modelo(),dados.anoModelo(),false,dados.combustivel(),mes,dados.valorCentavos(),"PARALLELUM");
        when(fornecedor.consultar(consulta)).thenReturn(dados);
        when(catalogo.salvarConsulta(dados,consulta.chave())).thenReturn(referencia);
        assertThat(new ResolucaoSobDemandaFipe(opcoes,fornecedor,catalogo,true).buscar(consulta)).contains(referencia);
        var ordem=inOrder(opcoes,fornecedor,catalogo);
        ordem.verify(opcoes).validar(consulta);
        ordem.verify(fornecedor).consultar(consulta);
        ordem.verify(catalogo).salvarConsulta(dados,consulta.chave());
    }
}
