package repasse.phcauto.backend.fipe.internal.core.gateway;
import java.util.*;
import java.time.LocalDate;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
public interface CatalogoFipeGateway {
    Optional<ReferenciaFipe> buscar(UUID id);
    Optional<ReferenciaFipe> buscarConsulta(String chave, LocalDate mes);
    ReferenciaFipe salvarConsulta(DadosReferencia dados, String chave);
    List<ReferenciaFipe> pesquisar(String tipo, String marca, String termo, Integer ano,
            LocalDate mes, int pagina, int tamanho);
}
