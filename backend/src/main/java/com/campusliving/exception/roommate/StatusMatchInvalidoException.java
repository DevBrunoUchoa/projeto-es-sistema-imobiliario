package com.campusliving.exception.roommate;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class StatusMatchInvalidoException extends ProjectException {
    public StatusMatchInvalidoException(String motivo) {
        super("Status de match invalido: " + motivo, HttpStatus.CONFLICT);
    }
}
