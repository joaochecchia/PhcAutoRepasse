package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response;
import java.util.List;
public record LojistasResponse(String mensagem, List<PerfilPublicoResponse> lojistas, long total,
        int pagina, int tamanho, Integer raioKmAplicado, boolean temProximaPagina) { }
