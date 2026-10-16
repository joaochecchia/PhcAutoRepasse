package repasse.phcauto.backend.fipe.internal.infrastructure.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaExterna;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.gateway.ReferenciaSobDemandaFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.JpaOpcoesFipeGateway;

@Service
public class ResolucaoSobDemandaFipe implements ReferenciaSobDemandaFipeGateway {
    private final JpaOpcoesFipeGateway opcoes;
    private final ParallelumClient fornecedor;
    private final CatalogoFipeGateway catalogo;
    private final boolean permitido;

    public ResolucaoSobDemandaFipe(JpaOpcoesFipeGateway opcoes,ParallelumClient fornecedor,
            CatalogoFipeGateway catalogo,@Value("${app.fipe.persistence-authorized:false}") boolean permitido) {
        this.opcoes=opcoes;this.fornecedor=fornecedor;this.catalogo=catalogo;this.permitido=permitido;
    }

    @Override public Optional<ReferenciaFipe> buscar(ConsultaExterna consulta) {
        if(!permitido)return Optional.empty();
        opcoes.validar(consulta);
        // HTTP fora da transação; a gravação usa a transação curta do gateway JPA.
        var dados=fornecedor.consultar(consulta);
        return Optional.of(catalogo.salvarConsulta(dados,consulta.chave()));
    }
}
