package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out;
import java.util.UUID;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.fipe.FipeFacade;
import repasse.phcauto.backend.anuncios.internal.core.domain.ReferenciaFipeVeiculo;
import repasse.phcauto.backend.anuncios.internal.core.gateway.ConsultarFipeGateway;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.domain.model.catalogo.FipeVeiculo;
@Component
public class FipeAnuncioAdapter implements ConsultarFipeGateway {
    private final FipeFacade fipe;
    public FipeAnuncioAdapter(FipeFacade fipe){this.fipe=fipe;}
    public ReferenciaFipeVeiculo buscar(UUID id){
        var r=fipe.buscarReferencia(id);
        if(r==null)throw new AnuncioInvalidoException("Referência FIPE indisponível");
        return new ReferenciaFipeVeiculo(new FipeVeiculo(r.id(),r.codigoFipe(),r.valorCentavos(),r.mesReferencia(),r.modelo(),r.fonte()),
                r.tipo(),r.marca(),r.modelo(),r.anoModelo(),r.zeroKm(),r.combustivel());
    }
}
