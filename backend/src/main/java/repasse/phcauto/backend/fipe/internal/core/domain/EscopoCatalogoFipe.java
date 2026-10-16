package repasse.phcauto.backend.fipe.internal.core.domain;
public final class EscopoCatalogoFipe {
    private EscopoCatalogoFipe() {}
    public static String chave(ConsultaOpcoesFipe c) {
        if(c.tipo()==null) return "references";
        return c.referencia()+":"+c.tipo()+(c.marcaId()==null?"":":"+c.marcaId())+(c.modeloId()==null?"":":"+c.modeloId());
    }
    public static String pai(ConsultaOpcoesFipe c) {
        if(c.tipo()==null) return null;
        if(c.marcaId()==null) return "references";
        return c.referencia()+":"+c.tipo()+(c.modeloId()==null?"":":"+c.marcaId());
    }
    public static String codigoPai(ConsultaOpcoesFipe c) {
        if(c.tipo()==null) return null;
        return c.modeloId()!=null?c.modeloId():c.marcaId()!=null?c.marcaId():c.referencia().toString();
    }
}
