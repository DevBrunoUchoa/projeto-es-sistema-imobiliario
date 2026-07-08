package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ProjectException {
    public UserNotFoundException() {
        super("O Usuario consultado nao existe!", HttpStatus.NOT_FOUND);
    }
}
