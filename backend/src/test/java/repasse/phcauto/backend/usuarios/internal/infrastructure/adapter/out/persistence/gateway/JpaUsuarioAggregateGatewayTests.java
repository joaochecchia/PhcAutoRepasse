package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.*;

class JpaUsuarioAggregateGatewayTests {
    @Test
    void contaSemSenhaLocalNaoPodeDefinirSenhaPeloFluxoDeAlteracao() {
        var usuarios = mock(UsuarioWriteRepository.class);
        var usuario = mock(UsuarioEntity.class);
        var usuarioId = UUID.randomUUID();
        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuario.possuiSenhaLocal()).thenReturn(false);
        var gateway = new JpaUsuarioAggregateGateway(usuarios,
                mock(UsuarioPfWriteRepository.class), mock(UsuarioPjWriteRepository.class),
                mock(EnderecoUsuarioWriteRepository.class));

        assertThrows(CadastroInvalidoException.class,
                () -> gateway.redefinir(usuarioId, "hash-novo", Instant.now()));

        verify(usuario, never()).redefinirSenha(anyString(), any());
        verify(usuarios, never()).saveAndFlush(any());
    }
}
