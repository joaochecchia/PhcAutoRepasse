package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;
@Repository
public class ConsultarArquivoPerfil {
    private final UsuarioWriteRepository usuarios;
    public ConsultarArquivoPerfil(UsuarioWriteRepository usuarios) { this.usuarios = usuarios; }
    @Transactional(transactionManager="writeTransactionManager", readOnly=true)
    public String chave(UUID id) {
        return usuarios.findById(id).filter(u -> u.getAtivo()).map(u -> u.getFotoPerfilChave()).orElse(null);
    }
}
