package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// Distinta de exception.interacao.AnuncioNaoEncontradoException (T5.4, usada
// só na checagem de existência de anúncio para favoritos/interesses) — esta
// aqui é a do domínio de gerenciamento de anúncios em si (T5.5).
public class AnuncioNaoEncontradoException extends ProjectException {
    public AnuncioNaoEncontradoException() {
        super("Anuncio nao encontrado.", HttpStatus.NOT_FOUND);
    }
}
