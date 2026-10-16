package repasse.phcauto.backend.anuncios.internal.core.usecase;
import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.core.domain.*;
import repasse.phcauto.backend.anuncios.internal.core.gateway.ConsultarFipeGateway;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.domain.model.catalogo.*;
/** Associação explícita. Não tenta inferir versão por similaridade ou por preço. */
public final class VincularFipeVeiculo implements VincularFipeVeiculoUseCase {
    private final ConsultarFipeGateway referencias;
    public VincularFipeVeiculo(ConsultarFipeGateway referencias){this.referencias=referencias;}
    public CriarAnuncioCommand execute(CriarAnuncioCommand c,UUID id){
        if(id==null)return c.comFipe(null);
        if(c.tipoVeiculo()==TipoVeiculo.BARCO || c.tipoVeiculo()==TipoVeiculo.LINHA_AMARELA)
            throw new AnuncioInvalidoException("Este tipo de veículo não aceita referência FIPE");
        var r=referencias.buscar(id);
        String tipo=c.tipoVeiculo()==TipoVeiculo.CAMINHONETE?"CARRO":c.tipoVeiculo().name();
        if(!tipo.equals(r.tipo()) || r.zeroKm()!=(c.condicao()==CondicaoVeiculo.ZERO_KM)
                || (!r.zeroKm() && !java.util.Objects.equals(c.anoModelo(),r.anoModelo())))
            throw new AnuncioInvalidoException("Tipo, condição ou ano do veículo incompatível com a referência FIPE");
        if(!marca(c.fabricante()).equals(marca(r.marca())))
            throw new AnuncioInvalidoException("Fabricante incompatível com a referência FIPE");
        // A versão deve reproduzir a descrição escolhida no catálogo. Textos aproximados nunca bastam.
        if(!normalizar(c.versao()).equals(normalizar(r.modelo())))
            throw new AnuncioInvalidoException("Informe em versao a descrição exata do modelo FIPE selecionado");
        String modelo=normalizar(c.modelo()), descricao=normalizar(r.modelo());
        if(modelo.isEmpty() || !(descricao.equals(modelo) || descricao.startsWith(modelo+" ")))
            throw new AnuncioInvalidoException("Modelo incompatível com a versão FIPE selecionada");
        String combustivel=normalizar(c.combustivel()), esperado=normalizar(r.combustivel());
        // A FIPE classifica muitos flex como Gasolina; só admite FLEX quando a descrição explicita essa tecnologia.
        boolean flex=combustivel.equals("FLEX") && (esperado.equals("FLEX")
                || (esperado.equals("GASOLINA") && (descricao.contains("FLEX") || descricao.contains("FLEXONE"))));
        if(!combustivel.equals(esperado) && !flex)
            throw new AnuncioInvalidoException("Combustível incompatível com a referência FIPE");
        return c.comFipe(r.snapshot());
    }
    private static String marca(String s){
        String n=normalizar(s);
        return switch(n){case "VW","VW VOLKSWAGEN","VOLKSWAGEN"->"VOLKSWAGEN";
            case "GM CHEVROLET","CHEVROLET","GM"->"CHEVROLET";
            case "MERCEDES","MB","MERCEDES BENZ"->"MERCEDES BENZ";
            case "VW CAMINHOES","VOLKSWAGEN CAMINHOES E ONIBUS"->"VOLKSWAGEN";default->n;};
    }
    private static String normalizar(String s){return s==null?"":java.text.Normalizer.normalize(s,java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}","").toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]+"," ").trim();}
}
