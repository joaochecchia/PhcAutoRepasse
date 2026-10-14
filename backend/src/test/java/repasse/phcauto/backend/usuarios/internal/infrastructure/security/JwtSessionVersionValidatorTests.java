package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;

class JwtSessionVersionValidatorTests {
    private final UsuarioWriteRepository usuarios = mock(UsuarioWriteRepository.class);
    private final JwtSessionVersionValidator validator = new JwtSessionVersionValidator(usuarios);
    private final UUID usuarioId = UUID.randomUUID();

    @Test
    void aceitaSomenteVersaoAtualDeUsuarioAtivo() {
        UsuarioEntity usuario = mock(UsuarioEntity.class);
        when(usuario.getAtivo()).thenReturn(true);
        when(usuario.getSessaoVersao()).thenReturn(3L);
        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertFalse(validator.validate(token(3L)).hasErrors());
        assertTrue(validator.validate(token(2L)).hasErrors());
    }

    @Test
    void rejeitaTokenAntigoSemVersaoOuUsuarioInativo() {
        UsuarioEntity usuario = mock(UsuarioEntity.class);
        when(usuario.getAtivo()).thenReturn(false);
        when(usuario.getSessaoVersao()).thenReturn(0L);
        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertTrue(validator.validate(token(null)).hasErrors());
        assertTrue(validator.validate(token(0L)).hasErrors());
    }

    private Jwt token(Long versao) {
        var claims = new java.util.HashMap<String, Object>();
        claims.put("sub", usuarioId.toString());
        if (versao != null) claims.put("sessionVersion", versao);
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"), claims);
    }
}
