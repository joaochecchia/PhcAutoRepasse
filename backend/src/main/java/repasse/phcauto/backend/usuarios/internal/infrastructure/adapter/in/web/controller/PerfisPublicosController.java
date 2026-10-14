package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.core.usecase.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.BuscarLojistasRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.*;
@RestController
@RequestMapping("/api/v1")
public class PerfisPublicosController {
    private final BuscarLojistasUseCase lojas;
    private final ConsultarPerfilPublicoUseCase perfis;
    public PerfisPublicosController(BuscarLojistasUseCase lojas, ConsultarPerfilPublicoUseCase perfis) {
        this.lojas = lojas; this.perfis = perfis;
    }
    @GetMapping("/lojistas")
    public LojistasResponse buscar(@Valid @ParameterObject BuscarLojistasRequest filtro) {
        var p = lojas.execute(filtro.filtro());
        return new LojistasResponse("Lojistas encontrados com sucesso", p.lojistas().stream().map(PerfilPublicoResponse::from).toList(),
                p.total(), p.pagina(), p.tamanho(), p.raioKmAplicado(), p.temProximaPagina());
    }
    @GetMapping("/perfis/{id}")
    public PerfilPublicoResponse perfil(@PathVariable UUID id) { return PerfilPublicoResponse.from(perfis.execute(id)); }
}
