package repasse.phcauto.backend.fipe.internal.core.usecase;

import java.util.Set;
import repasse.phcauto.backend.fipe.internal.core.domain.OpcaoFipe;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.gateway.EdicaoAtivaFipeGateway;

public final class BuscarEdicaoAtivaFipeUseCase {
    private final EdicaoAtivaFipeGateway gateway;
    public BuscarEdicaoAtivaFipeUseCase(EdicaoAtivaFipeGateway gateway) { this.gateway=gateway; }
    public OpcaoFipe execute(String tipo) {
        if(tipo==null || !Set.of("cars","motorcycles","trucks").contains(tipo))
            throw new FipeException(FipeException.Motivo.INVALIDA,"Tipo FIPE inválido");
        return gateway.buscar(tipo).orElseThrow(()->new FipeException(FipeException.Motivo.NAO_ENCONTRADA,
            "Catálogo FIPE ainda não disponível. Continue o cadastro manualmente."));
    }
}
