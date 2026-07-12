package com.campusliving.service.imovel;
import org.springframework.web.multipart.MultipartFile;
public interface ImageStorageService {
    StoredImage upload(String path, MultipartFile file);
    void delete(String path);
    record StoredImage(String path, String publicUrl) {}
}
