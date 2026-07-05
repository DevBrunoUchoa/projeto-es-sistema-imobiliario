package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;

public class UserNotFoundException extends ProjectException {
    public UserNotFoundException() {
        super("O Usuario consultado nao existe!");
    }
}
