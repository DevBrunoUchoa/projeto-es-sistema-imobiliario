package com.campusliving.exception.roommate;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class PerfilRoommateNaoEncontradoException extends ProjectException {
    public PerfilRoommateNaoEncontradoException() {
        super("Crie seu perfil de roommate (POST /roommates/perfil) antes de buscar compativeis.", HttpStatus.NOT_FOUND);
    }
}
