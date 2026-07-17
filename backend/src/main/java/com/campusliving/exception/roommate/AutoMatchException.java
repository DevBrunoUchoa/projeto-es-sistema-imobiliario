package com.campusliving.exception.roommate;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class AutoMatchException extends ProjectException {
    public AutoMatchException() {
        super("Nao e possivel solicitar match com voce mesmo.", HttpStatus.BAD_REQUEST);
    }
}
