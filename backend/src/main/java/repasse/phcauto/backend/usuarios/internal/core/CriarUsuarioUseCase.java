package repasse.phcauto.backend.usuarios.internal.core;

public interface CriarUsuarioUseCase {
    UsuarioCriadoResultado execute(CriarUsuarioCommand command);
}
