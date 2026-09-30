package repasse.phcauto.backend.anuncios.internal.infrastructure.security;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write.AnuncioWriteRepository;

@Component("anuncioAuthorization")
public class AnuncioAuthorization {
    private final AnuncioWriteRepository anuncios;
    public AnuncioAuthorization(AnuncioWriteRepository anuncios) { this.anuncios = anuncios; }
    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public boolean podeAlterar(UUID id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return true;
        return anuncios.findById(id).map(a -> a.getAnuncianteId().toString().equals(auth.getName())).orElse(false);
    }
}
