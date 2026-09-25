package repasse.phcauto.backend.usuarios.internal.core;

import repasse.phcauto.backend.usuarios.request.LoginRequest;

public interface LoginUseCase {
    void execute(LoginRequest request);
}
