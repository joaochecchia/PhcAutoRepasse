package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway;
public final class ConsultarOpcoesFipe implements ConsultarOpcoesFipeUseCase {
    private final OpcoesFipeGateway gateway;
    public ConsultarOpcoesFipe(OpcoesFipeGateway gateway){this.gateway=gateway;}
    public PaginaOpcoesFipe execute(ConsultaOpcoesFipe c){
        return gateway.pagina(c);
    }
}
