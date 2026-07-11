package com.campusliving.exception.roommate;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class MatchNaoEncontradoException extends ProjectException {
    public MatchNaoEncontradoException() {
        super("A solicitacao de match consultada nao existe.", HttpStatus.NOT_FOUND);
    }
}
