package com.campusliving.exception.roommate;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-34: já existe uma solicitação PENDENTE ou ACEITA entre o par, em
// qualquer sentido (A->B ou B->A).
public class MatchDuplicadoException extends ProjectException {
    public MatchDuplicadoException() {
        super("Ja existe uma solicitacao de match pendente ou aceita entre voces.", HttpStatus.CONFLICT);
    }
}
