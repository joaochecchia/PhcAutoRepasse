package repasse.phcauto.backend.usuarios.internal.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.core.LoginUseCase;
import repasse.phcauto.backend.usuarios.request.LoginRequest;
import repasse.phcauto.backend.usuarios.response.LoginResponse;

@RestController
@RequestMapping("/api/v1/usuarios/login")
public class LoginController {
    private final LoginUseCase login;

    public LoginController(LoginUseCase login) { this.login = login; }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        login.execute(request);
        return ResponseEntity.ok(new LoginResponse("Login realizado com sucesso"));
    }
}
