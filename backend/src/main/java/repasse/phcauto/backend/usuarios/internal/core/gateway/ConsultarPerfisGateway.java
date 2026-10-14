package repasse.phcauto.backend.usuarios.internal.core.gateway;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.core.domain.*;
public interface ConsultarPerfisGateway {
    PaginaLojistas buscarLojistas(FiltroLojistas filtro);
    Optional<PerfilPublico> buscarPerfil(UUID id);
}
