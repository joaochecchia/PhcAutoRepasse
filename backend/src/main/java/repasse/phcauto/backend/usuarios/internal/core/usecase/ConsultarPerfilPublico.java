package repasse.phcauto.backend.usuarios.internal.core.usecase;
import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.core.domain.PerfilPublico;
import repasse.phcauto.backend.usuarios.internal.core.gateway.ConsultarPerfisGateway;
import repasse.phcauto.backend.usuarios.internal.core.exception.UsuarioNaoEncontradoException;
public final class ConsultarPerfilPublico implements ConsultarPerfilPublicoUseCase {
    private final ConsultarPerfisGateway gateway;
    public ConsultarPerfilPublico(ConsultarPerfisGateway gateway) { this.gateway = gateway; }
    public PerfilPublico execute(UUID id) { return gateway.buscarPerfil(id).orElseThrow(UsuarioNaoEncontradoException::new); }
}
