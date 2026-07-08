package com.campusliving.exception.interacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// Usada por favoritos/interesses ao referenciar um anuncio (ads.id) que nao
// existe. O Ad/Anuncio ainda nao tem entidade JPA propria (isso e' T5.5), por
// isso a checagem de existencia e' feita via query nativa nos repositories de
// Favorito/Contato em vez de uma FK gerenciada pelo Hibernate.
public class AnuncioNaoEncontradoException extends ProjectException {
    public AnuncioNaoEncontradoException() {
        super("O anuncio referenciado nao existe.", HttpStatus.NOT_FOUND);
    }
}
