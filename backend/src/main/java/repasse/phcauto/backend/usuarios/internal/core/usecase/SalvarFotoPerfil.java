package repasse.phcauto.backend.usuarios.internal.core.usecase;
import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.core.gateway.SalvarFotoPerfilGateway;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
public final class SalvarFotoPerfil implements SalvarFotoPerfilUseCase {
    private final SalvarFotoPerfilGateway gateway;
    public SalvarFotoPerfil(SalvarFotoPerfilGateway gateway) { this.gateway = gateway; }
    public String execute(UUID id, String chave) {
        if (id == null || chave == null || !chave.matches("usuarios/" + id + "/perfil/[a-f0-9-]{36}\\.jpg"))
            throw new CadastroInvalidoException("Referência da foto inválida");
        return gateway.salvar(id, chave);
    }
}
