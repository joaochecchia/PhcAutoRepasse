package repasse.phcauto.backend.localizacao.internal.core.domain;

public record Municipio(
        int codigoIbge,
        String nome,
        String uf,
        double latitude,
        double longitude) {

    public Municipio {
        if (codigoIbge < 1_000_000 || codigoIbge > 9_999_999) {
            throw new IllegalArgumentException("Código IBGE inválido");
        }
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do município obrigatório");
        if (uf == null || !uf.matches("[A-Z]{2}")) throw new IllegalArgumentException("UF inválida");
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas municipais inválidas");
        }
    }
}
