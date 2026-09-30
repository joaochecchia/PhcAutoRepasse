package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.JwtLoginService;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.LoginRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.LoginResponse;

@RestController
@RequestMapping("/api/v1/usuarios/login")
public class LoginController {
    private final JwtLoginService login;

    public LoginController(JwtLoginService login) { this.login = login; }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(login.execute(request));
    }
}
