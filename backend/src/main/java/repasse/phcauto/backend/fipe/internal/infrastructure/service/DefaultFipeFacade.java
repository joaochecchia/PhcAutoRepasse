package repasse.phcauto.backend.fipe.internal.infrastructure.service;
import java.util.UUID;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.fipe.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
@Service
public class DefaultFipeFacade implements FipeFacade {
    private final CatalogoFipeGateway catalogo;
    public DefaultFipeFacade(CatalogoFipeGateway catalogo){this.catalogo=catalogo;}
    public ReferenciaFipe buscarReferencia(UUID id){return catalogo.buscar(id).orElse(null);}
}
