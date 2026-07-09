package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

public class ImovelNaoEncontradoException extends ProjectException {
    public ImovelNaoEncontradoException() {
        super("Imovel nao encontrado.", HttpStatus.NOT_FOUND);
    }
}
