package com.campusliving.exception.avaliacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-29: bloquear mais de uma avaliação por par (avaliador, anuncio). Também
// é garantido no banco por uq_reviews_avaliador_ad (V13__create_reviews.sql)
// — esta exception cobre o caminho feliz (checagem prévia no service); ver
// AvaliacaoServiceImpl para o tratamento do caso de corrida entre requisições
// concorrentes, onde quem "perde a corrida" recebe essa mesma exception via
// DataIntegrityViolationException.
public class AvaliacaoDuplicadaException extends ProjectException {
    public AvaliacaoDuplicadaException() {
        super("Voce ja avaliou este anuncio.", HttpStatus.CONFLICT);
    }
}