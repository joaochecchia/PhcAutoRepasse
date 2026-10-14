package repasse.phcauto.backend.usuarios.internal.core.usecase;
import repasse.phcauto.backend.usuarios.internal.core.domain.*;
import repasse.phcauto.backend.usuarios.internal.core.gateway.ConsultarPerfisGateway;
public final class BuscarLojistas implements BuscarLojistasUseCase {
    private final ConsultarPerfisGateway gateway;
    public BuscarLojistas(ConsultarPerfisGateway gateway) { this.gateway = gateway; }
    public PaginaLojistas execute(FiltroLojistas filtro) { return gateway.buscarLojistas(filtro); }
}
