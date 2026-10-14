package repasse.phcauto.backend.usuarios.internal.core.domain;
import java.util.List;
public record PaginaLojistas(List<PerfilPublico> lojistas, long total, int pagina, int tamanho,
        Integer raioKmAplicado) {
    public PaginaLojistas { lojistas = List.copyOf(lojistas); }
    public boolean temProximaPagina() { return ((long) pagina + 1) * tamanho < total; }
}
