package com.campusliving.exception.roommate;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-32, fluxo secundário: campos obrigatórios em branco impedem a ativação
// do perfil público (nível de barulho e horário que costuma dormir).
public class PerfilRoommateIncompletoException extends ProjectException {
    public PerfilRoommateIncompletoException() {
        super("Preencha o nivel de barulho e o horario que costuma dormir antes de tornar seu perfil publico.", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
