package repasse.phcauto.backend.anuncios;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.AnuncioEntity;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write.AnuncioWriteRepository;
import repasse.phcauto.backend.anuncios.internal.infrastructure.security.AnuncioAuthorization;

class AnuncioAuthorizationTests {
    @Test
    void somenteProprietarioOuDonoPodeAlterarAnuncio() {
        var repository = mock(AnuncioWriteRepository.class);
        var authorization = new AnuncioAuthorization(repository);
        var anuncioId = UUID.randomUUID();
        var proprietarioId = UUID.randomUUID();
        var outroId = UUID.randomUUID();
        var anuncio = mock(AnuncioEntity.class);
        when(anuncio.getAnuncianteId()).thenReturn(proprietarioId);
        when(repository.findById(anuncioId)).thenReturn(Optional.of(anuncio));

        assertTrue(authorization.podeAlterar(anuncioId, autenticacao(proprietarioId, "CLIENTE")));
        assertFalse(authorization.podeAlterar(anuncioId, autenticacao(outroId, "CLIENTE")));
        assertFalse(authorization.podeAlterar(anuncioId, autenticacao(outroId, "ADMIN")));
        assertTrue(authorization.podeAlterar(anuncioId, autenticacao(outroId, "DONO")));
    }

    private UsernamePasswordAuthenticationToken autenticacao(UUID usuarioId, String papel) {
        return new UsernamePasswordAuthenticationToken(usuarioId.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + papel)));
    }
}
