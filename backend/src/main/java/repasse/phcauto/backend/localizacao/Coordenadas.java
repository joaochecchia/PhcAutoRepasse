package repasse.phcauto.backend.localizacao;

public record Coordenadas(double latitude, double longitude) {
    public Coordenadas {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas geográficas inválidas");
        }
    }
}
