package com.campusliving.exception.imovel;
import org.springframework.http.HttpStatus;
import com.campusliving.exception.ProjectException;
public class ImagemNaoEncontradaException extends ProjectException {
    public ImagemNaoEncontradaException() { super("Imagem nao encontrada", HttpStatus.NOT_FOUND); }
}
