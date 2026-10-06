package repasse.phcauto.backend.localizacao.internal.infrastructure.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repasse.phcauto.backend.localizacao.LocalizacaoFacade;
import repasse.phcauto.backend.localizacao.internal.infrastructure.adapter.in.web.response.CoordenadasMunicipioResponse;

@Validated
@RestController
@RequestMapping("/api/v1/localizacao")
@Tag(name = "Localização (teste)", description = "Consultas temporárias ao catálogo local de municípios")
public class LocalizacaoTesteController {

    private final LocalizacaoFacade localizacaoFacade;

    public LocalizacaoTesteController(LocalizacaoFacade localizacaoFacade) {
        this.localizacaoFacade = localizacaoFacade;
    }

    @GetMapping("/municipios/coordenadas")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @Operation(
            summary = "Consulta as coordenadas centrais de um município",
            description = "Endpoint temporário de teste. Exige cidade e UF para evitar ambiguidade entre municípios homônimos.")
    public ResponseEntity<CoordenadasMunicipioResponse> buscarCoordenadas(
            @RequestParam @NotBlank @Size(max = 150) String cidade,
            @RequestParam @Pattern(regexp = "(?i)[A-Z]{2}", message = "UF deve conter exatamente duas letras") String uf) {
        var municipio = localizacaoFacade.buscarMunicipio(cidade, uf);
        return ResponseEntity.ok(CoordenadasMunicipioResponse.from(municipio));
    }
}
