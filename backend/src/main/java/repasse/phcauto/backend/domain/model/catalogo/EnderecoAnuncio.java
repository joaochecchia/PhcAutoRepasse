package repasse.phcauto.backend.domain.model.catalogo;

import java.util.UUID;

/** Contrato de domínio da localização pertencente a um anúncio. */
public abstract class EnderecoAnuncio {
    public abstract UUID getId();
    /** Opcional para preservar anúncios legados; novos fluxos podem exigi-lo. */
    public abstract String getCep();
    public abstract String getCidade();
    /** Opcional para preservar anúncios legados; novos fluxos podem exigi-lo. */
    public abstract String getBairro();
    /** Opcional para preservar anúncios legados; novos fluxos podem exigi-lo. */
    public abstract String getRua();
    public abstract String getNumero();
    public abstract String getComplemento();
    public abstract String getUf();
    public abstract Integer getMunicipioCodigoIbge();
}
