package repasse.phcauto.backend.fipe.internal.core.domain;
public final class EscopoCatalogoFipe {
    private EscopoCatalogoFipe() {}
    public static String chave(ConsultaOpcoesFipe c) {
        if(c.tipo()==null) return "references";
        String marca=c.referencia()+":"+c.tipo()+(c.marcaId()==null?"":":"+c.marcaId());
        if(c.anosDaMarca()) return marca+":anos";
        if(c.anoId()!=null) return marca+":ano:"+c.anoId();
        return marca+(c.modeloId()==null?"":":"+c.modeloId());
    }
    public static String pai(ConsultaOpcoesFipe c) {
        if(c.tipo()==null) return null;
        if(c.marcaId()==null) return "references";
        // Os modelos de um ano/combustível pendem do ano na lista de anos da marca.
        if(c.anoId()!=null) return c.referencia()+":"+c.tipo()+":"+c.marcaId()+":anos";
        return c.referencia()+":"+c.tipo()+(c.modeloId()==null?"":":"+c.marcaId());
    }
    public static String codigoPai(ConsultaOpcoesFipe c) {
        if(c.tipo()==null) return null;
        if(c.anoId()!=null) return c.anoId();
        return c.modeloId()!=null?c.modeloId():c.marcaId()!=null?c.marcaId():c.referencia().toString();
    }
}
