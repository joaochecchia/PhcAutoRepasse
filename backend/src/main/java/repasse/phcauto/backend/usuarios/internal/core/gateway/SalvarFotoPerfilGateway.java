package repasse.phcauto.backend.usuarios.internal.core.gateway;
import java.util.UUID;
public interface SalvarFotoPerfilGateway {
    String salvar(UUID usuarioId, String chave);
}
