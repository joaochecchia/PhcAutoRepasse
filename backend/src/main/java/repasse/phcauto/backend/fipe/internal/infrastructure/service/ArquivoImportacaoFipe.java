package repasse.phcauto.backend.fipe.internal.infrastructure.service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import tools.jackson.databind.ObjectMapper;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
/** Leitura limitada por linha e por lote, mantendo offset em bytes para retomada. */
public final class ArquivoImportacaoFipe {
    private ArquivoImportacaoFipe() { }
    public record Lote(List<DadosReferencia> dados,long posicao,boolean fim) { }
    public static Lote ler(Path arquivo,long inicio,ObjectMapper json) throws IOException {
        var dados=new ArrayList<DadosReferencia>();long posicao=inicio;boolean fim=false;
        try(var in=new BufferedInputStream(Files.newInputStream(arquivo))){
            in.skipNBytes(inicio);
            while(dados.size()<100){
                var linha=new ByteArrayOutputStream();int b;
                while((b=in.read())!=-1){posicao++;if(b=='\n')break;linha.write(b);
                    if(linha.size()>8192)throw new IOException("Linha FIPE excede 8 KB");}
                if(b==-1)fim=true;
                if(linha.size()>0){String texto=linha.toString(StandardCharsets.UTF_8).trim();
                    if(!texto.isEmpty())dados.add(json.readValue(texto,DadosReferencia.class));}
                if(fim)break;
                // Limita também arquivos compostos por linhas vazias.
                if(posicao-inicio>1_000_000)break;
            }
        }
        return new Lote(List.copyOf(dados),posicao,fim);
    }
}
