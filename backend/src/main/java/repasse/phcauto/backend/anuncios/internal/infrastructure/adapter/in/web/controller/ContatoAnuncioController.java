package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.CriarAnuncioRequest.ContatoAnuncianteRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.service.ContatoAnuncioService;

@RestController
@RequestMapping("/api/v1/anuncios/{anuncioId}/contato")
public class ContatoAnuncioController {
    private final ContatoAnuncioService contatos;
    public ContatoAnuncioController(ContatoAnuncioService contatos) { this.contatos = contatos; }

    @PostMapping("/whatsapp")
    public ContatoAnuncioService.ContatoLiberado whatsapp(@PathVariable UUID anuncioId,
            Authentication authentication, HttpServletRequest request) {
        return contatos.liberarWhatsapp(anuncioId, UUID.fromString(authentication.getName()),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @org.springframework.security.access.prepost.PreAuthorize("@anuncioAuthorization.podeAlterar(#anuncioId, authentication)")
    @PutMapping("/preferencias")
    public void preferencias(@PathVariable UUID anuncioId, @Valid @RequestBody ContatoAnuncianteRequest request,
            Authentication authentication) {
        contatos.salvarPreferencias(anuncioId, UUID.fromString(authentication.getName()), request);
    }
}
