package com.campusliving.exception.avaliacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-29, Fluxo Secundário 1: "Comentário com palavras de baixo calão — filtro
// automático bloqueia e adverte usuário." Ver PalavraoFilter para a lista de
// termos e a lógica de detecção.
public class ComentarioImproprioException extends ProjectException {
    public ComentarioImproprioException() {
        super("Seu comentario contem termos impropios e nao pode ser publicado. Revise o texto e tente novamente.", HttpStatus.BAD_REQUEST);
    }
}