package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RNF/SEG-05: documento de verificação precisa ser imagem ou PDF, até 5MB.
public class DocumentoInvalidoException extends ProjectException {
    public DocumentoInvalidoException(String motivo) {
        super("Documento invalido: " + motivo, HttpStatus.BAD_REQUEST);
    }
}
