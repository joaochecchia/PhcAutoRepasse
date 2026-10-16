package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.controller;
import java.util.*;
import java.time.LocalDate;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.fipe.FipeFacade;
import repasse.phcauto.backend.fipe.internal.core.domain.PesquisaFipe;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.usecase.*;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.request.*;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response.*;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaOpcoesFipe;
@RestController @RequestMapping("/api/v1/fipe")
public class FipeController {
    private final PesquisarFipeUseCase pesquisar;
    private final ResolverReferenciaFipeUseCase resolver;
    private final FipeFacade facade;
    private final ConsultarOpcoesFipeUseCase fornecedor;
    private final BuscarSugestoesFipeUseCase sugestoes;
    private final BuscarEdicaoAtivaFipeUseCase edicaoAtiva;
    public FipeController(PesquisarFipeUseCase pesquisar,ResolverReferenciaFipeUseCase resolver,FipeFacade facade,
            ConsultarOpcoesFipeUseCase fornecedor,BuscarSugestoesFipeUseCase sugestoes,BuscarEdicaoAtivaFipeUseCase edicaoAtiva){this.pesquisar=pesquisar;this.resolver=resolver;this.facade=facade;this.fornecedor=fornecedor;this.sugestoes=sugestoes;this.edicaoAtiva=edicaoAtiva;}
    @GetMapping("/catalogo/edicao-ativa") public OpcoesFipeResponse.OpcaoResponse edicaoAtiva(@RequestParam String tipo){
        var o=edicaoAtiva.execute(tipo);return new OpcoesFipeResponse.OpcaoResponse(o.codigo(),o.nome(),o.mesReferencia());
    }
    @GetMapping("/tipos") public List<OpcoesFipeResponse.OpcaoResponse> tipos(){
        return List.of(new OpcoesFipeResponse.OpcaoResponse("cars","Carros e caminhonetes",null),
            new OpcoesFipeResponse.OpcaoResponse("motorcycles","Motos",null),new OpcoesFipeResponse.OpcaoResponse("trucks","Caminhões",null));
    }
    @GetMapping("/referencias")
    public List<ReferenciaFipeResponse> buscar(@RequestParam(required=false)String tipo,
            @RequestParam(required=false)String marca,@RequestParam(required=false)String termo,
            @RequestParam(required=false)Integer ano,@RequestParam(required=false)LocalDate mes,
            @RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="50")int tamanho){
        return pesquisar.execute(new PesquisaFipe(tipo,marca,termo,ano,mes,pagina,tamanho)).stream().map(ReferenciaFipeResponse::de).toList();
    }
    @GetMapping("/referencias/{id}") public ReferenciaFipeResponse buscar(@PathVariable UUID id){
        var r=facade.buscarReferencia(id);if(r==null)throw new FipeException(FipeException.Motivo.NAO_ENCONTRADA,"Referência não encontrada");
        return ReferenciaFipeResponse.de(r);
    }
    @PostMapping("/consultas") public ReferenciaFipeResponse resolver(@Valid @RequestBody ResolverFipeRequest request){return ReferenciaFipeResponse.de(resolver.execute(request.comando()));}
    @PostMapping("/sugestoes") public List<SugestaoFipeResponse> sugerir(@Valid @RequestBody BuscarSugestoesFipeRequest request){
        return sugestoes.execute(request.comando()).stream().map(SugestaoFipeResponse::de).toList();
    }
    @GetMapping({"/catalogo/opcoes", "/fornecedor/opcoes"})
    public OpcoesFipeResponse opcoes(@RequestParam(required=false)String tipo,
            @RequestParam(required=false)String marcaId,@RequestParam(required=false)String modeloId,
            @RequestParam(required=false)Integer referencia,@RequestParam(defaultValue="")String termo,
            @RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="50")int tamanho){
        var resultado=fornecedor.execute(new ConsultaOpcoesFipe(tipo,marcaId,modeloId,referencia,termo,pagina,tamanho));
        return new OpcoesFipeResponse(resultado.opcoes().stream().map(o->new OpcoesFipeResponse.OpcaoResponse(o.codigo(),o.nome(),o.mesReferencia())).toList(),
                resultado.pagina(),resultado.tamanho(),resultado.temProximaPagina(),resultado.catalogoPendente());
    }
}
