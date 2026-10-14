package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioNaoEncontradoException;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.FotoAnuncioResponse;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.entity.FotoEntity;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write.AnuncioWriteRepository;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.out.persistence.repository.write.FotoWriteRepository;

@Service
public class FotoAnuncioService {
    private static final int MAXIMO_FOTOS_POR_VEICULO = 8;
    private static final long MAXIMO_PIXELS = 16_000_000L;
    private static final double FATOR_REDUCAO = 0.85;
    private final Path raiz;
    private final long tamanhoMaximo;
    private final long tamanhoAlvo;
    private final FotoWriteRepository fotos;
    private final AnuncioWriteRepository anuncios;

    public FotoAnuncioService(@Value("${app.storage.fotos.diretorio:./uploads}") String diretorio,
            @Value("${app.storage.fotos.tamanho-maximo-bytes:10485760}") long tamanhoMaximo,
            @Value("${app.storage.fotos.tamanho-alvo-bytes:5242880}") long tamanhoAlvo,
            FotoWriteRepository fotos, AnuncioWriteRepository anuncios) {
        if (tamanhoAlvo < 1 || tamanhoAlvo > tamanhoMaximo) {
            throw new IllegalArgumentException("O tamanho alvo da foto deve ser positivo e não superar o limite de upload");
        }
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
        this.tamanhoMaximo = tamanhoMaximo;
        this.tamanhoAlvo = tamanhoAlvo;
        this.fotos = fotos;
        this.anuncios = anuncios;
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public FotoAnuncioResponse salvar(UUID anuncioId, MultipartFile arquivo, int posicao, String textoAlternativo) {
        var anuncio = anuncios.findById(anuncioId).orElseThrow(() -> new AnuncioNaoEncontradoException(anuncioId));
        if (fotos.countByAnuncioId(anuncioId) >= MAXIMO_FOTOS_POR_VEICULO) {
            throw new AnuncioInvalidoException("Cada veículo pode ter no máximo 8 fotos");
        }
        if (arquivo == null || arquivo.isEmpty()) throw new AnuncioInvalidoException("A foto é obrigatória");
        if (arquivo.getSize() > tamanhoMaximo) throw new AnuncioInvalidoException("A foto excede o limite permitido");
        if (posicao < 0 || posicao >= MAXIMO_FOTOS_POR_VEICULO) {
            throw new AnuncioInvalidoException("A posição da foto deve estar entre 0 e 7");
        }
        byte[] conteudo;
        try { conteudo = arquivo.getBytes(); }
        catch (IOException e) { throw new AnuncioInvalidoException("Não foi possível ler a foto"); }
        ImagemProcessada imagem = processar(conteudo, tamanhoAlvo);
        conteudo = imagem.conteudo();
        String extensao = imagem.extensao();
        UUID id = UUID.randomUUID();
        String chave = "usuarios/" + anuncio.getAnuncianteId()
                + "/veiculos/" + anuncio.getVeiculoId() + "/" + id + extensao;
        Path destino = resolver(chave);
        try {
            Files.createDirectories(destino.getParent());
            Path temporario = Files.createTempFile(destino.getParent(), ".upload-", ".tmp");
            try {
                Files.write(temporario, conteudo, StandardOpenOption.TRUNCATE_EXISTING);
                Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE);
            } finally { Files.deleteIfExists(temporario); }
            var foto = fotos.saveAndFlush(FotoEntity.nova(id, anuncioId, chave, posicao, textoAlternativo));
            removerArquivoSeTransacaoFalhar(destino);
            return response(foto);
        } catch (RuntimeException | IOException e) {
            try { Files.deleteIfExists(destino); } catch (IOException ignored) { }
            if (e instanceof AnuncioInvalidoException invalida) throw invalida;
            throw new AnuncioInvalidoException("Não foi possível armazenar a foto");
        }
    }

    private void removerArquivoSeTransacaoFalhar(Path destino) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try { Files.deleteIfExists(destino); } catch (IOException ignored) { }
                }
            }
        });
    }

    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public List<FotoAnuncioResponse> listar(UUID anuncioId) {
        if (!anuncios.existsById(anuncioId)) throw new AnuncioNaoEncontradoException(anuncioId);
        return fotos.findByAnuncioId(anuncioId).stream()
                .sorted(Comparator.comparingInt(FotoEntity::getPosicao)).map(this::response).toList();
    }

    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public ArquivoFoto carregar(UUID anuncioId, UUID fotoId) {
        var foto = fotos.findById(fotoId).filter(f -> f.getAnuncioId().equals(anuncioId))
                .orElseThrow(() -> new AnuncioInvalidoException("Foto não encontrada"));
        try {
            Resource recurso = new UrlResource(resolver(foto.getChaveArquivo()).toUri());
            if (!recurso.exists() || !recurso.isReadable()) throw new AnuncioInvalidoException("Arquivo da foto não encontrado");
            return new ArquivoFoto(recurso, tipo(foto.getChaveArquivo()));
        } catch (java.net.MalformedURLException e) { throw new AnuncioInvalidoException("Caminho da foto inválido"); }
    }

    static ImagemProcessada processar(byte[] bytes, long tamanhoAlvo) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (stream == null) throw imagemInvalida();
            Iterator<ImageReader> leitores = ImageIO.getImageReaders(stream);
            if (!leitores.hasNext()) throw imagemInvalida();
            ImageReader leitor = leitores.next();
            try {
                String formato = leitor.getFormatName().toLowerCase();
                if (!formato.equals("jpeg") && !formato.equals("jpg") && !formato.equals("png")) {
                    throw new AnuncioInvalidoException("Formato permitido: JPEG ou PNG");
                }
                leitor.setInput(stream, true, true);
                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if (largura < 1 || altura < 1 || (long) largura * altura > MAXIMO_PIXELS) {
                    throw new AnuncioInvalidoException("A foto deve ter no máximo 16 megapixels");
                }
                if (bytes.length <= tamanhoAlvo) {
                    return new ImagemProcessada(bytes, formato.equals("png") ? ".png" : ".jpg");
                }
                BufferedImage original = leitor.read(0);
                return reduzir(original, formato.equals("png"), tamanhoAlvo);
            } finally {
                leitor.dispose();
            }
        } catch (AnuncioInvalidoException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw imagemInvalida();
        }
    }

    private static ImagemProcessada reduzir(BufferedImage original, boolean png, long tamanhoAlvo) throws IOException {
        BufferedImage atual = original;
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            byte[] resultado = png ? escreverPng(atual) : escreverJpeg(atual, 0.85f);
            if (resultado.length <= tamanhoAlvo) {
                return new ImagemProcessada(resultado, png ? ".png" : ".jpg");
            }
            int largura = Math.max(1, (int) Math.floor(atual.getWidth() * FATOR_REDUCAO));
            int altura = Math.max(1, (int) Math.floor(atual.getHeight() * FATOR_REDUCAO));
            if (largura == atual.getWidth() && altura == atual.getHeight()) break;
            atual = redimensionar(atual, largura, altura, png);
        }
        throw new AnuncioInvalidoException("Não foi possível reduzir a foto para até 5 MB");
    }

    private static BufferedImage redimensionar(BufferedImage origem, int largura, int altura, boolean png) {
        int tipo = png ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage destino = new BufferedImage(largura, altura, tipo);
        Graphics2D graphics = destino.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(origem, 0, 0, largura, altura, null);
        } finally {
            graphics.dispose();
        }
        return destino;
    }

    private static byte[] escreverPng(BufferedImage imagem) throws IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        if (!ImageIO.write(imagem, "png", saida)) throw new IOException("Codificador PNG indisponível");
        return saida.toByteArray();
    }

    private static byte[] escreverJpeg(BufferedImage imagem, float qualidade) throws IOException {
        Iterator<ImageWriter> escritores = ImageIO.getImageWritersByFormatName("jpeg");
        if (!escritores.hasNext()) throw new IOException("Codificador JPEG indisponível");
        ImageWriter escritor = escritores.next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream saida = ImageIO.createImageOutputStream(bytes)) {
            escritor.setOutput(saida);
            ImageWriteParam parametros = escritor.getDefaultWriteParam();
            parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parametros.setCompressionQuality(qualidade);
            escritor.write(null, new javax.imageio.IIOImage(imagem, null, null), parametros);
        } finally {
            escritor.dispose();
        }
        return bytes.toByteArray();
    }

    private static AnuncioInvalidoException imagemInvalida() {
        return new AnuncioInvalidoException("O arquivo enviado não é uma imagem JPEG ou PNG válida");
    }

    private Path resolver(String chave) {
        Path caminho = raiz.resolve(chave).normalize();
        if (!caminho.startsWith(raiz)) throw new AnuncioInvalidoException("Caminho da foto inválido");
        return caminho;
    }
    private FotoAnuncioResponse response(FotoEntity f) {
        return new FotoAnuncioResponse(f.getId(), f.getAnuncioId(), f.getPosicao(), f.getTextoAlternativo(),
                "/api/v1/anuncios/" + f.getAnuncioId() + "/fotos/" + f.getId() + "/arquivo");
    }
    private String tipo(String chave) { return chave.endsWith(".png") ? "image/png" : "image/jpeg"; }
    record ImagemProcessada(byte[] conteudo, String extensao) { }
    public record ArquivoFoto(Resource recurso, String contentType) { }
}
