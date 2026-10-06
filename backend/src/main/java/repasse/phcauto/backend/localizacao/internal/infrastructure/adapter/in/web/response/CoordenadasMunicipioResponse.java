package repasse.phcauto.backend.localizacao.internal.infrastructure.adapter.in.web.response;

import repasse.phcauto.backend.localizacao.MunicipioLocalizacao;

public record CoordenadasMunicipioResponse(
        int codigoIbge,
        String cidade,
        String uf,
        double latitude,
        double longitude) {

    public static CoordenadasMunicipioResponse from(MunicipioLocalizacao municipio) {
        return new CoordenadasMunicipioResponse(
                municipio.codigoIbge(),
                municipio.nome(),
                municipio.uf(),
                municipio.latitude(),
                municipio.longitude());
    }
}
