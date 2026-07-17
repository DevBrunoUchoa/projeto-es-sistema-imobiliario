package com.campusliving.service.imovel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import com.campusliving.exception.imovel.StorageException;

@Service
public class SupabaseImageStorageService implements ImageStorageService {
    private final RestClient restClient;
    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final Path localDirectory;
    private final String publicBackendUrl;

    public SupabaseImageStorageService(
            RestClient.Builder builder,
            @Value("${app.storage.supabase.url:}") String supabaseUrl,
            @Value("${app.storage.supabase.service-key:}") String serviceKey,
            @Value("${app.storage.supabase.bucket:anuncios}") String bucket,
            @Value("${app.storage.local.directory:uploads}") String localDirectory,
            @Value("${app.backend.public-url:http://localhost:${APP_PORT:8080}}") String publicBackendUrl) {
        this.supabaseUrl = removeTrailingSlash(supabaseUrl);
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.localDirectory = Path.of(localDirectory).toAbsolutePath().normalize();
        this.publicBackendUrl = removeTrailingSlash(publicBackendUrl);
        this.restClient = builder.build();
    }

    @Override
    public StoredImage upload(String path, MultipartFile file) {
        if (!isSupabaseConfigured()) {
            return uploadLocally(path, file);
        }
        try {
            restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
            return new StoredImage(path, publicUrl);
        } catch (RestClientResponseException e) {
            throw new StorageException("Supabase recusou o upload (HTTP " + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            throw new StorageException("Nao foi possivel enviar a imagem ao Supabase Storage");
        }
    }

    @Override
    public void delete(String path) {
        if (!isSupabaseConfigured()) {
            deleteLocally(path);
            return;
        }
        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .body(Map.of("prefixes", List.of(path)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new StorageException("Supabase recusou a exclusao (HTTP " + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            throw new StorageException("Nao foi possivel excluir a imagem do Supabase Storage");
        }
    }

    private boolean isSupabaseConfigured() {
        return StringUtils.hasText(supabaseUrl) && StringUtils.hasText(serviceKey) && StringUtils.hasText(bucket);
    }

    private StoredImage uploadLocally(String path, MultipartFile file) {
        Path destination = localDirectory.resolve(path).normalize();
        if (!destination.startsWith(localDirectory)) {
            throw new StorageException("Caminho de upload invalido");
        }
        try {
            Files.createDirectories(destination.getParent());
            file.transferTo(destination);
            return new StoredImage(path, publicBackendUrl + "/uploads/" + path);
        } catch (IOException e) {
            throw new StorageException("Nao foi possivel salvar a imagem localmente");
        }
    }

    private void deleteLocally(String path) {
        Path destination = localDirectory.resolve(path).normalize();
        if (!destination.startsWith(localDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(destination);
        } catch (IOException e) {
            throw new StorageException("Nao foi possivel excluir a imagem local");
        }
    }

    private static String removeTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
