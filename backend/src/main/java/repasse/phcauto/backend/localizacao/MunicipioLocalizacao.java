package repasse.phcauto.backend.localizacao;

public record MunicipioLocalizacao(
        int codigoIbge,
        String nome,
        String uf,
        double latitude,
        double longitude) {

    public Coordenadas coordenadas() {
        return new Coordenadas(latitude, longitude);
    }
}
