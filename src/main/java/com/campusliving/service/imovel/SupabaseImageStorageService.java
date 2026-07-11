package com.campusliving.service.imovel;

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

    public SupabaseImageStorageService(
            RestClient.Builder builder,
            @Value("${app.storage.supabase.url:}") String supabaseUrl,
            @Value("${app.storage.supabase.service-key:}") String serviceKey,
            @Value("${app.storage.supabase.bucket:anuncios}") String bucket) {
        this.supabaseUrl = removeTrailingSlash(supabaseUrl);
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.restClient = builder.build();
    }

    @Override
    public StoredImage upload(String path, MultipartFile file) {
        validateConfiguration();
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
        validateConfiguration();
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

    private void validateConfiguration() {
        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(serviceKey) || !StringUtils.hasText(bucket)) {
            throw new StorageException("Supabase Storage nao esta configurado");
        }
    }

    private static String removeTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
