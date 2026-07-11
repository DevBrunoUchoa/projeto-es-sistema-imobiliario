package com.campusliving.exception.avaliacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-31: usada quando o review_id informado em PUT /avaliacoes/{id}/resposta
// não existe (ou já foi anonimizado/removido).
public class AvaliacaoNaoEncontradaException extends ProjectException {
    public AvaliacaoNaoEncontradaException() {
        super("A avaliacao consultada nao existe.", HttpStatus.NOT_FOUND);
    }
}