package repasse.phcauto.backend.fipe.internal.core.usecase;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaExterna;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.gateway.ReferenciaSobDemandaFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
/** Resolve a referência local; o adaptador decide se uma ausência permite consulta externa. */
public final class ResolverReferenciaFipe implements ResolverReferenciaFipeUseCase {
    private final CatalogoFipeGateway catalogo;
    private final ReferenciaSobDemandaFipeGateway sobDemanda;
    public ResolverReferenciaFipe(CatalogoFipeGateway catalogo,ReferenciaSobDemandaFipeGateway sobDemanda) {
        this.catalogo=catalogo;this.sobDemanda=sobDemanda;
    }
    public ReferenciaFipe execute(ConsultaExterna consulta) {
        return catalogo.buscarConsulta(consulta.chave(),consulta.mesReferencia())
            .or(()->sobDemanda.buscar(consulta)).orElseThrow(()->
            new FipeException(FipeException.Motivo.NAO_ENCONTRADA,"Referência ainda não disponível no catálogo local. Você pode continuar sem FIPE."));
    }
}
