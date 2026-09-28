package repasse.phcauto.backend.usuarios.internal.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write.UsuarioWriteRepository;

/** Mantém a transação aberta somente durante a leitura das credenciais. */
@Component
class UsuarioCredenciaisReader {
    private final UsuarioWriteRepository usuarios;

    UsuarioCredenciaisReader(UsuarioWriteRepository usuarios) { this.usuarios = usuarios; }

    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    Credenciais carregar(String email) {
        var candidatos = usuarios.findTop2ByEmailIgnoreCase(email);
        var usuario = candidatos.size() == 1 ? candidatos.get(0) : null;
        return usuario == null ? Credenciais.ausentes()
                : new Credenciais(true, usuario.getAtivo(), usuario.getSenhaHash());
    }

    record Credenciais(boolean encontrada, boolean ativa, String senhaHash) {
        static Credenciais ausentes() { return new Credenciais(false, false, null); }
    }
}
