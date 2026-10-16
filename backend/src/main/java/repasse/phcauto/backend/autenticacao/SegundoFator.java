package repasse.phcauto.backend.autenticacao;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.UsuarioSeguranca;

public interface SegundoFator {
    boolean habilitado();
    Desafio criar(UsuarioSeguranca usuario, String agente);
    void validar(String desafioId, String codigo, UUID usuarioId, long versaoSeguranca, String agente);
    UsuarioSeguranca validar(String desafioId, String codigo, String agente);

    record Desafio(String id, long expiresIn, String emailMascarado) { }
    class CodigoInvalidoException extends RuntimeException { }
}
