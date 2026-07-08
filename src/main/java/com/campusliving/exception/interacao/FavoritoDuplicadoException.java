package com.campusliving.exception.interacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class FavoritoDuplicadoException extends ProjectException {
    public FavoritoDuplicadoException() {
        super("Este anuncio ja esta nos favoritos deste usuario.", HttpStatus.CONFLICT);
    }
}
