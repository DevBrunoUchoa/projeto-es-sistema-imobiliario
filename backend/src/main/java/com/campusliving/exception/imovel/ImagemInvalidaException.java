package com.campusliving.exception.imovel;
import org.springframework.http.HttpStatus;
import com.campusliving.exception.ProjectException;
public class ImagemInvalidaException extends ProjectException {
    public ImagemInvalidaException(String mensagem) { super(mensagem, HttpStatus.BAD_REQUEST); }
}
