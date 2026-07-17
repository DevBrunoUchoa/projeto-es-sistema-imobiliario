package com.campusliving.exception.avaliacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// Espelha chk_reviews_nao_auto_avaliacao (V13__create_reviews.sql): o
// avaliador não pode ser o mesmo usuário do locador avaliado. Checar isso
// antes do INSERT dá uma mensagem de erro amigável em vez de deixar a
// aplicação estourar uma violação de CHECK constraint crua para o cliente.
public class AutoAvaliacaoException extends ProjectException {
    public AutoAvaliacaoException() {
        super("Voce nao pode avaliar o proprio anuncio.", HttpStatus.BAD_REQUEST);
    }
}