package repasse.phcauto.backend.compliance.internal.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repasse.phcauto.backend.compliance.internal.core.domain.VersoesDocumentosVigentes;

@RestController
@RequestMapping("/api/v1/compliance/documentos-vigentes")
public class DocumentosVigentesController {
    private final VersoesDocumentosVigentes versoes;

    public DocumentosVigentesController(VersoesDocumentosVigentes versoes) {
        this.versoes = versoes;
    }

    @GetMapping
    public DocumentosVigentesResponse buscar() {
        return new DocumentosVigentesResponse(versoes.termosUso(), versoes.politicaPrivacidade());
    }

    public record DocumentosVigentesResponse(String versaoTermosUso,
            String versaoPoliticaPrivacidade) { }
}
