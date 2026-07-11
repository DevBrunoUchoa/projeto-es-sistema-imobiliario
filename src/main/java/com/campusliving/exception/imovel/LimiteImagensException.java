package com.campusliving.exception.imovel;
import org.springframework.http.HttpStatus;
import com.campusliving.exception.ProjectException;
public class LimiteImagensException extends ProjectException {
    public LimiteImagensException() { super("Um anuncio pode ter no maximo 10 imagens", HttpStatus.CONFLICT); }
}
