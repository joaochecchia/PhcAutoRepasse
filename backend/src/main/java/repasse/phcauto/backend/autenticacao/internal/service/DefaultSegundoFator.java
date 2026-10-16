package repasse.phcauto.backend.autenticacao.internal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.autenticacao.SegundoFator;
import repasse.phcauto.backend.autenticacao.internal.security.SecurityHashes;
import repasse.phcauto.backend.usuarios.UsuarioSeguranca;
import repasse.phcauto.backend.usuarios.SegurancaUsuarios;

@Service
class DefaultSegundoFator implements SegundoFator {
    private final TwoFactorChallengeService desafios;
    private final SegurancaUsuarios usuarios;

    DefaultSegundoFator(TwoFactorChallengeService desafios, SegurancaUsuarios usuarios) {
        this.desafios = desafios; this.usuarios = usuarios;
    }

    @Override public boolean habilitado() { return desafios.enabled(); }

    @Override public Desafio criar(UsuarioSeguranca usuario, String agente) {
        var desafio = desafios.criar(usuario, agente);
        return new Desafio(desafio.challengeId(), desafio.expiresIn(), desafio.emailMascarado());
    }

    @Override public void validar(String desafioId, String codigo, UUID usuarioId,
            long versaoSeguranca, String agente) {
        try {
            var dados = desafios.validar(desafioId, codigo);
            String agenteHash = SecurityHashes.sha256(agente == null ? "desconhecido" : agente);
            boolean mesmoAgente = MessageDigest.isEqual(agenteHash.getBytes(StandardCharsets.UTF_8),
                    dados.agenteHash().getBytes(StandardCharsets.UTF_8));
            if (!dados.usuarioId().equals(usuarioId) || dados.versao() != versaoSeguranca || !mesmoAgente) {
                throw new CodigoInvalidoException();
            }
        } catch (TwoFactorChallengeService.CodigoInvalidoException error) {
            throw new CodigoInvalidoException();
        }
    }

    @Override public UsuarioSeguranca validar(String desafioId, String codigo, String agente) {
        try {
            var dados = desafios.validar(desafioId, codigo);
            var usuario = usuarios.buscarAtivo(dados.usuarioId());
            validarDados(dados.usuarioId(), dados.versao(), dados.agenteHash(), usuario.id(),
                    usuario.versaoSeguranca(), agente);
            return usuario;
        } catch (TwoFactorChallengeService.CodigoInvalidoException error) {
            throw new CodigoInvalidoException();
        }
    }

    private static void validarDados(UUID desafioUsuario, long desafioVersao, String desafioAgente,
            UUID usuarioId, long versaoSeguranca, String agente) {
        String agenteHash = SecurityHashes.sha256(agente == null ? "desconhecido" : agente);
        boolean mesmoAgente = MessageDigest.isEqual(agenteHash.getBytes(StandardCharsets.UTF_8),
                desafioAgente.getBytes(StandardCharsets.UTF_8));
        if (!desafioUsuario.equals(usuarioId) || desafioVersao != versaoSeguranca || !mesmoAgente) {
            throw new CodigoInvalidoException();
        }
    }
}
