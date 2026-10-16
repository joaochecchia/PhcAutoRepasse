package repasse.phcauto.backend.usuarios;

import java.util.UUID;

public record UsuarioSeguranca(UUID id, String email, String papel, long versaoSeguranca,
        boolean possuiSenhaLocal) { }
