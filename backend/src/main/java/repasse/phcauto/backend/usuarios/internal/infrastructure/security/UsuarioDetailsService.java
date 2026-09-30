package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.util.Locale;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;

/** Materializa as credenciais na transação; a verificação BCrypt ocorre fora dela. */
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioWriteRepository usuarios;
    public UsuarioDetailsService(UsuarioWriteRepository usuarios) { this.usuarios = usuarios; }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        var encontrados = usuarios.findTop2ByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT));
        if (encontrados.size() != 1) throw new UsernameNotFoundException("Credenciais inválidas");
        var usuario = encontrados.get(0);
        if (usuario.getSenhaHash() == null) throw new UsernameNotFoundException("Credenciais inválidas");
        return new User(usuario.getId().toString(), usuario.getSenhaHash(), usuario.getAtivo(),
                true, true, true, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPapel().name())));
    }
}
