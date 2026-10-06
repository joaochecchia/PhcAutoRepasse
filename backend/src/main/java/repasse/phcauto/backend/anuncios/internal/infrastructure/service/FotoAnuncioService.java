package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
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
    private final Path raiz;
    private final long tamanhoMaximo;
    private final FotoWriteRepository fotos;
    private final AnuncioWriteRepository anuncios;

    public FotoAnuncioService(@Value("${app.storage.fotos.diretorio:./uploads}") String diretorio,
            @Value("${app.storage.fotos.tamanho-maximo-bytes:10485760}") long tamanhoMaximo,
            FotoWriteRepository fotos, AnuncioWriteRepository anuncios) {
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
        this.tamanhoMaximo = tamanhoMaximo;
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
        String extensao = detectarExtensao(conteudo, arquivo.getContentType());
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

    private String detectarExtensao(byte[] bytes, String contentType) {
        try { if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) throw new IOException(); }
        catch (IOException e) { throw new AnuncioInvalidoException("O arquivo enviado não é uma imagem válida"); }
        return switch (contentType == null ? "" : contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new AnuncioInvalidoException("Formato permitido: JPEG ou PNG");
        };
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
    public record ArquivoFoto(Resource recurso, String contentType) { }
}
