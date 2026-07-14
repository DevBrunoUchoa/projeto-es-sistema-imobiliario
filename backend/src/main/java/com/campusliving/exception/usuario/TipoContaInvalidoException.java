package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-18: só ESTUDANTE pode virar MISTO (LOCADOR/MISTO/ADMIN já não se
// qualificam para essa promoção).
public class TipoContaInvalidoException extends ProjectException {
    public TipoContaInvalidoException() {
        super("Somente contas do tipo ESTUDANTE podem virar conta mista.", HttpStatus.CONFLICT);
    }
}
