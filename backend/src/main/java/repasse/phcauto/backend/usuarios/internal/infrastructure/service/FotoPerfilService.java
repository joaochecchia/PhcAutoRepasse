package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.io.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import repasse.phcauto.backend.usuarios.internal.core.usecase.SalvarFotoPerfilUseCase;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.gateway.ConsultarArquivoPerfil;

@Service
public class FotoPerfilService {
    private final Path raiz;
    private final SalvarFotoPerfilUseCase salvarFoto;
    private final ConsultarArquivoPerfil consulta;
    private final TransactionTemplate transacao;
    public FotoPerfilService(@Value("${app.storage.fotos.diretorio:./uploads}") String diretorio,
            SalvarFotoPerfilUseCase salvarFoto, ConsultarArquivoPerfil consulta,
            @Qualifier("writeTransactionManager") PlatformTransactionManager manager) {
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
        this.salvarFoto = salvarFoto; this.consulta = consulta; transacao = new TransactionTemplate(manager);
    }
    public void salvar(UUID id, MultipartFile arquivo) {
        byte[] jpeg = converter(arquivo);
        String chave = "usuarios/" + id + "/perfil/" + UUID.randomUUID() + ".jpg";
        Path destino = resolver(chave);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, jpeg, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) { apagar(destino); throw new IllegalStateException("Não foi possível salvar a foto", e); }
        String anterior;
        try { anterior = transacao.execute(status -> salvarFoto.execute(id, chave)); }
        catch (RuntimeException e) { apagar(destino); throw e; }
        if (anterior != null) apagar(resolver(anterior));
    }
    public Resource carregar(UUID id) {
        String chave = consulta.chave(id);
        if (chave == null) return null;
        var arquivo = new FileSystemResource(resolver(chave));
        return arquivo.isReadable() ? arquivo : null;
    }
    private Path resolver(String chave) {
        Path caminho = raiz.resolve(chave).normalize();
        if (!caminho.startsWith(raiz)) throw new IllegalArgumentException("Caminho inválido");
        return caminho;
    }
    private static void apagar(Path arquivo) {
        try { Files.deleteIfExists(arquivo); }
        catch (IOException e) { org.slf4j.LoggerFactory.getLogger(FotoPerfilService.class).warn("Falha ao remover foto de perfil obsoleta", e); }
    }
    static byte[] converter(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty() || arquivo.getSize() > 5 * 1024 * 1024)
            throw new CadastroInvalidoException("Envie uma foto JPEG ou PNG de até 5 MB");
        try (var stream = ImageIO.createImageInputStream(arquivo.getInputStream())) {
            var leitores = ImageIO.getImageReaders(stream);
            if (!leitores.hasNext()) throw new CadastroInvalidoException("Foto inválida: use JPEG ou PNG");
            var leitor = leitores.next();
            try {
                String formato = leitor.getFormatName();
                if (!formato.equalsIgnoreCase("JPEG") && !formato.equalsIgnoreCase("PNG"))
                    throw new CadastroInvalidoException("Formato não permitido: use JPEG ou PNG");
                leitor.setInput(stream);
                int w = leitor.getWidth(0), h = leitor.getHeight(0);
                if (w < 1 || h < 1 || (long) w * h > 16000000)
                    throw new CadastroInvalidoException("Foto deve ter no máximo 16 megapixels");
                var original = leitor.read(0);
                double escala = Math.min(1.0, 800.0 / Math.max(w, h));
                var imagem = new BufferedImage(Math.max(1, (int)(w*escala)), Math.max(1, (int)(h*escala)), BufferedImage.TYPE_INT_RGB);
                var g = imagem.createGraphics();
                try { g.setColor(Color.WHITE); g.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
                    g.drawImage(original, 0, 0, imagem.getWidth(), imagem.getHeight(), null); }
                finally { g.dispose(); }
                var resultado = new ByteArrayOutputStream();
                if (!ImageIO.write(imagem, "jpeg", resultado)) throw new IOException("JPEG indisponível");
                return resultado.toByteArray();
            } finally { leitor.dispose(); }
        } catch (IOException e) { throw new CadastroInvalidoException("Não foi possível ler a foto: use JPEG ou PNG válido"); }
    }
}
