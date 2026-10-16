package repasse.phcauto.backend.anuncios.internal.core.usecase;
import java.util.*;
import repasse.phcauto.backend.anuncios.internal.core.domain.*;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.domain.model.catalogo.FipeVeiculo;
/** Regras de manutenção da referência independentes de JPA e HTTP. */
public final class ReconciliarFipeVeiculo implements ReconciliarFipeVeiculoUseCase {
    private final VincularFipeVeiculoUseCase vincular;
    public ReconciliarFipeVeiculo(VincularFipeVeiculoUseCase vincular){this.vincular=vincular;}
    public CriarAnuncioCommand execute(CriarAnuncioCommand a,CriarAnuncioCommand c,AtualizarAnuncioCommand p,FipeVeiculo snapshot){
        if(p.removerFipe() && p.fipeReferenciaId()!=null)
            throw new AnuncioInvalidoException("Não é possível remover e selecionar FIPE ao mesmo tempo");
        if(p.removerFipe())return c.comFipe(null);
        if(p.fipeReferenciaId()!=null)return vincular.execute(c,p.fipeReferenciaId());
        boolean mudou=!Objects.equals(c.fabricante(),a.fabricante()) || !Objects.equals(c.modelo(),a.modelo())
                || !Objects.equals(c.versao(),a.versao()) || !Objects.equals(c.anoModelo(),a.anoModelo())
                || c.condicao()!=a.condicao() || !Objects.equals(c.combustivel(),a.combustivel())
                || !tecnica(c.detalhes()).equals(tecnica(a.detalhes()));
        return c.comFipe(mudou?null:snapshot);
    }
    private static List<?> tecnica(CriarAnuncioCommand.DetalhesVeiculo d){
        if(d instanceof CriarAnuncioCommand.Carro c)return Arrays.asList(c.cambio(),c.motorizacao(),c.cilindradaLitros(),c.carroceria(),c.numeroPortas());
        if(d instanceof CriarAnuncioCommand.Moto c)return Arrays.asList(c.cambio(),c.cilindradas(),c.categoria());
        if(d instanceof CriarAnuncioCommand.Caminhao c)return Arrays.asList(c.cambio(),c.configuracao(),c.numeroEixos());
        if(d instanceof CriarAnuncioCommand.Caminhonete c)return Arrays.asList(c.cambio(),c.motorizacao(),c.cilindradaLitros(),c.tipoCabine(),c.numeroPortas());
        return List.of();
    }
}
