package com.campusliving.service.usuario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.campusliving.exception.usuario.DocumentoInvalidoException;

/**
 * Implementação interina: grava no disco local (dentro do container/host).
 *
 * <p>Isso funciona para desenvolvimento e para validar o fluxo de ponta a
 * ponta, mas NÃO é adequado para produção em hosts com filesystem efêmero
 * (ex.: Render free tier reinicia o container e perde o que foi escrito em
 * disco). O T5.9 deve substituir esta classe por uma implementação que fale
 * com um object storage de verdade (ex.: Supabase Storage) — a interface
 * {@link DocumentStorageService} já isola essa troca do resto do código.</p>
 */
@Service
public class LocalDiskDocumentStorageService implements DocumentStorageService {

    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024; // RNF/SEG-05: 5MB
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "application/pdf", "image/png", "image/jpeg", "image/webp"
    );

    private final Path diretorioBase;

    public LocalDiskDocumentStorageService(@Value("${app.uploads.dir:uploads}") String diretorioBaseConfig) {
        this.diretorioBase = Path.of(diretorioBaseConfig);
    }

    @Override
    public String salvar(MultipartFile arquivo, String subpasta) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new DocumentoInvalidoException("arquivo vazio ou nao enviado");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new DocumentoInvalidoException("tamanho maximo permitido e 5MB");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new DocumentoInvalidoException("tipo de arquivo nao permitido (use PDF, PNG, JPEG ou WEBP)");
        }

        try {
            Path pasta = diretorioBase.resolve(subpasta);
            Files.createDirectories(pasta);

            String extensao = extensaoParaContentType(contentType);
            String nomeArquivo = UUID.randomUUID() + extensao;
            Path destino = pasta.resolve(nomeArquivo);

            arquivo.transferTo(destino);

            // Caminho relativo apenas — ainda não há um endpoint de serving
            // estático nem CDN configurados (isso também é escopo do T5.9).
            return "/" + diretorioBase + "/" + subpasta + "/" + nomeArquivo;
        } catch (IOException e) {
            throw new DocumentoInvalidoException("falha ao salvar o arquivo: " + e.getMessage());
        }
    }

    private String extensaoParaContentType(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
