package com.campusliving.controller.imovel;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.campusliving.dto.imovel.ImagemAnuncioResponseDTO;
import com.campusliving.service.imovel.ImagemAnuncioService;

@RestController
@RequestMapping("/anuncios/{adId}/imagens")
public class ImagemAnuncioController {
    private final ImagemAnuncioService service;
    public ImagemAnuncioController(ImagemAnuncioService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImagemAnuncioResponseDTO>> upload(@PathVariable UUID adId,
            @RequestPart("imagens") List<MultipartFile> imagens,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(adId, imagens, requesterId));
    }

    @GetMapping
    public List<ImagemAnuncioResponseDTO> list(@PathVariable UUID adId) { return service.list(adId); }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID adId, @PathVariable UUID imageId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        service.delete(adId, imageId, requesterId); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/principal")
    public ImagemAnuncioResponseDTO setMain(@PathVariable UUID adId, @PathVariable UUID imageId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        return service.setMain(adId, imageId, requesterId);
    }
}
