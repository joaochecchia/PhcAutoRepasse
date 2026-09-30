package repasse.phcauto.backend.usuarios.internal.core.usecase;

import repasse.phcauto.backend.usuarios.internal.core.domain.LoginCommand;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioAutenticado;
public interface LoginUseCase {
    UsuarioAutenticado execute(LoginCommand request);
}
