package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;

public class EmailEmUsoException extends ProjectException {
    public EmailEmUsoException() {
        super("O email inserido ja entá em uso!");
    }
}
