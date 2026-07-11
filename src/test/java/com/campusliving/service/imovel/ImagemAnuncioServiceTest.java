package com.campusliving.service.imovel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import com.campusliving.exception.imovel.*;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.imovel.*;
import com.campusliving.repository.imovel.*;

@ExtendWith(MockitoExtension.class)
class ImagemAnuncioServiceTest {
    @Mock AnuncioRepository anuncioRepository;
    @Mock ImagemAnuncioRepository imagemRepository;
    @Mock ImageStorageService storageService;
    ImagemAnuncioService service;
    UUID owner = UUID.randomUUID(), adId = UUID.randomUUID();

    @BeforeEach void setup() { service = new ImagemAnuncioService(anuncioRepository, imagemRepository, storageService); }

    @Test void uploadsFirstImageAsMain() {
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(ad()));
        when(imagemRepository.countByAdId(adId)).thenReturn(0L);
        when(storageService.upload(any(), any())).thenReturn(new ImageStorageService.StoredImage("path.jpg", "https://image"));
        when(imagemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = service.upload(adId, List.of(jpeg()), owner);
        assertEquals(1, result.size()); assertEquals("https://image", result.get(0).getUrl());
        assertTrue(result.get(0).isPrincipal());
    }

    @Test void rejectsSpoofedOwner() {
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(ad()));
        assertThrows(AcessoNegadoException.class, () -> service.upload(adId, List.of(jpeg()), UUID.randomUUID()));
        verify(storageService, never()).upload(any(), any());
    }

    @Test void enforcesTenImageLimit() {
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(ad()));
        when(imagemRepository.countByAdId(adId)).thenReturn(10L);
        assertThrows(LimiteImagensException.class, () -> service.upload(adId, List.of(jpeg()), owner));
    }

    @Test void rejectsFakeJpegContent() {
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(ad()));
        when(imagemRepository.countByAdId(adId)).thenReturn(0L);
        var fake = new MockMultipartFile("imagens", "fake.jpg", "image/jpeg", "not an image".getBytes());
        assertThrows(ImagemInvalidaException.class, () -> service.upload(adId, List.of(fake), owner));
    }

    @Test void deleteRemovesStorageObjectAndMetadata() {
        UUID imageId = UUID.randomUUID();
        ImagemAnuncio image = ImagemAnuncio.builder().id(imageId).adId(adId).storagePath("a/b.jpg")
                .url("https://image").ordem(0).principal(true).build();
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(ad()));
        when(imagemRepository.findByIdAndAdId(imageId, adId)).thenReturn(Optional.of(image));
        when(imagemRepository.findByAdIdOrderByOrdemAsc(adId)).thenReturn(List.of());
        service.delete(adId, imageId, owner);
        verify(storageService).delete("a/b.jpg"); verify(imagemRepository).delete(image);
    }

    private Anuncio ad() { return Anuncio.builder().id(adId).locadorId(owner).build(); }
    private MockMultipartFile jpeg() { return new MockMultipartFile("imagens", "foto.jpg", "image/jpeg",
            new byte[] {(byte)0xFF, (byte)0xD8, (byte)0xFF, 0}); }
}
