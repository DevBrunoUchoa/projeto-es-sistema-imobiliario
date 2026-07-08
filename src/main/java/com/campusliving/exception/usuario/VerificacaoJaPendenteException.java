package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class VerificacaoJaPendenteException extends ProjectException {
    public VerificacaoJaPendenteException() {
        super("Ja existe uma solicitacao de verificacao PENDENTE para este usuario.", HttpStatus.CONFLICT);
    }
}
