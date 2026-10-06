package repasse.phcauto.backend.localizacao;

public interface LocalizacaoFacade {
    MunicipioLocalizacao buscarMunicipio(String cidade, String uf);
    MunicipioLocalizacao buscarMunicipio(int codigoIbge);
}
