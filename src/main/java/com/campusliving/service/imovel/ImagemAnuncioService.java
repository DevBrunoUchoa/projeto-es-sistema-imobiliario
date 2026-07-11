package com.campusliving.service.imovel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.campusliving.dto.imovel.ImagemAnuncioResponseDTO;
import com.campusliving.exception.imovel.ImagemInvalidaException;
import com.campusliving.exception.imovel.ImagemNaoEncontradaException;
import com.campusliving.exception.imovel.LimiteImagensException;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.imovel.ImagemAnuncio;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImagemAnuncioRepository;

@Service
public class ImagemAnuncioService {
    static final int MAX_IMAGES = 10;
    static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final AnuncioRepository anuncioRepository;
    private final ImagemAnuncioRepository imagemRepository;
    private final ImageStorageService storageService;

    public ImagemAnuncioService(AnuncioRepository anuncioRepository,
            ImagemAnuncioRepository imagemRepository, ImageStorageService storageService) {
        this.anuncioRepository = anuncioRepository;
        this.imagemRepository = imagemRepository;
        this.storageService = storageService;
    }

    @Transactional
    public List<ImagemAnuncioResponseDTO> upload(UUID adId, List<MultipartFile> files, UUID requesterId) {
        Anuncio anuncio = getOwnedAd(adId, requesterId);
        if (files == null || files.isEmpty()) throw new ImagemInvalidaException("Envie ao menos uma imagem");

        long existing = imagemRepository.countByAdId(adId);
        if (existing + files.size() > MAX_IMAGES) throw new LimiteImagensException();

        files.forEach(this::validateImage);
        List<ImageStorageService.StoredImage> uploaded = new ArrayList<>();
        List<ImagemAnuncio> entities = new ArrayList<>();
        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String path = anuncio.getLocadorId() + "/" + adId + "/" + UUID.randomUUID() + extension(file);
                ImageStorageService.StoredImage stored = storageService.upload(path, file);
                uploaded.add(stored);
                entities.add(ImagemAnuncio.builder()
                        .adId(adId).url(stored.publicUrl()).storagePath(stored.path())
                        .ordem(Math.toIntExact(existing + i))
                        .principal(existing == 0 && i == 0).build());
            }
            return imagemRepository.saveAll(entities).stream().map(ImagemAnuncioResponseDTO::new).toList();
        } catch (RuntimeException e) {
            uploaded.forEach(item -> {
                try { storageService.delete(item.path()); } catch (RuntimeException ignored) { }
            });
            throw e;
        }
    }

    public List<ImagemAnuncioResponseDTO> list(UUID adId) {
        if (!anuncioRepository.existsById(adId)) throw new AnuncioNaoEncontradoException();
        return imagemRepository.findByAdIdOrderByOrdemAsc(adId).stream()
                .map(ImagemAnuncioResponseDTO::new).toList();
    }

    @Transactional
    public void delete(UUID adId, UUID imageId, UUID requesterId) {
        getOwnedAd(adId, requesterId);
        ImagemAnuncio image = imagemRepository.findByIdAndAdId(imageId, adId)
                .orElseThrow(ImagemNaoEncontradaException::new);
        storageService.delete(image.getStoragePath());
        boolean wasMain = image.isPrincipal();
        imagemRepository.delete(image);
        imagemRepository.flush();
        List<ImagemAnuncio> remaining = imagemRepository.findByAdIdOrderByOrdemAsc(adId);
        for (int i = 0; i < remaining.size(); i++) remaining.get(i).setOrdem(i);
        if (wasMain && !remaining.isEmpty()) remaining.get(0).setPrincipal(true);
        imagemRepository.saveAll(remaining);
    }

    @Transactional
    public ImagemAnuncioResponseDTO setMain(UUID adId, UUID imageId, UUID requesterId) {
        getOwnedAd(adId, requesterId);
        ImagemAnuncio selected = imagemRepository.findByIdAndAdId(imageId, adId)
                .orElseThrow(ImagemNaoEncontradaException::new);
        imagemRepository.clearPrincipalByAdId(adId);
        selected.setPrincipal(true);
        imagemRepository.save(selected);
        return new ImagemAnuncioResponseDTO(selected);
    }

    private Anuncio getOwnedAd(UUID adId, UUID requesterId) {
        if (requesterId == null) throw new AcessoNegadoException();
        Anuncio ad = anuncioRepository.findById(adId).orElseThrow(AnuncioNaoEncontradoException::new);
        if (!requesterId.equals(ad.getLocadorId())) throw new AcessoNegadoException();
        return ad;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ImagemInvalidaException("Arquivo de imagem vazio");
        if (file.getSize() > MAX_FILE_SIZE) throw new ImagemInvalidaException("Cada imagem deve ter no maximo 5 MB");
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!List.of("image/jpeg", "image/png", "image/webp").contains(type))
            throw new ImagemInvalidaException("Formato permitido: JPEG, PNG ou WEBP");
        try {
            byte[] bytes = file.getBytes();
            if (!matchesSignature(bytes, type)) throw new ImagemInvalidaException("O conteudo nao corresponde ao formato informado");
        } catch (IOException e) { throw new ImagemInvalidaException("Nao foi possivel ler a imagem"); }
    }

    private boolean matchesSignature(byte[] b, String type) {
        if (type.equals("image/jpeg")) return b.length >= 3 && (b[0]&255)==0xFF && (b[1]&255)==0xD8 && (b[2]&255)==0xFF;
        if (type.equals("image/png")) return b.length >= 8 && (b[0]&255)==0x89 && b[1]==0x50 && b[2]==0x4E && b[3]==0x47;
        return b.length >= 12 && b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F' && b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P';
    }

    private String extension(MultipartFile file) {
        return switch (file.getContentType()) { case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> ".jpg"; };
    }
}
