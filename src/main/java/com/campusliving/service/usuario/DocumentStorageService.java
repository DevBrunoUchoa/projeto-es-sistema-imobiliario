package com.campusliving.service.usuario;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstração para onde os documentos de verificação de locador (RF-08/09) são
 * salvos. Existe uma única implementação hoje (disco local, interina — ver
 * {@link LocalDiskDocumentStorageService}); quando o T5.9 (upload/storage de
 * imagens) definir a integração real com armazenamento em nuvem, é essa
 * interface que deve trocar de implementação, sem mexer no service de
 * usuário.
 */
public interface DocumentStorageService {

    /**
     * Valida e salva o arquivo, retornando uma URL/caminho para gravar em
     * {@code verificacao_locador.documento_url}.
     *
     * @throws com.campusliving.exception.usuario.DocumentoInvalidoException
     *         se o arquivo estiver vazio, for grande demais ou não for
     *         imagem/PDF (RNF/SEG-05).
     */
    String salvar(MultipartFile arquivo, String subpasta);
}
