package repasse.phcauto.backend.usuarios.internal.core.domain;
public record FiltroLojistas(String nome, String cidade, String uf,
        Double latitude, Double longitude, int pagina, int tamanho) {
    public FiltroLojistas(String nome, String cidade, String uf, int pagina, int tamanho) {
        this(nome, cidade, uf, null, null, pagina, tamanho);
    }
    public FiltroLojistas {
        nome = limpar(nome); cidade = limpar(cidade); uf = limpar(uf);
        if (pagina < 0 || tamanho < 1 || tamanho > 12) throw new IllegalArgumentException("Paginação inválida");
        if (nome != null && nome.length() > 160 || cidade != null && cidade.length() > 120)
            throw new IllegalArgumentException("Filtro muito longo");
        if (uf != null && !uf.matches("(?i)[A-Z]{2}")) throw new IllegalArgumentException("UF inválida");
        if ((latitude == null) != (longitude == null))
            throw new IllegalArgumentException("Latitude e longitude devem ser informadas juntas");
        if ((cidade == null) != (uf == null))
            throw new IllegalArgumentException("Cidade e UF devem ser informadas juntas");
        if (latitude != null && cidade != null)
            throw new IllegalArgumentException("Informe coordenadas ou cidade e UF, não ambos");
        if (latitude != null && (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180))
            throw new IllegalArgumentException("Coordenadas inválidas");
    }
    public boolean temLocalizacao() { return latitude != null || cidade != null; }
    private static String limpar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
