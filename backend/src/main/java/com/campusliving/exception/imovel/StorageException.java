package com.campusliving.exception.imovel;
import org.springframework.http.HttpStatus;
import com.campusliving.exception.ProjectException;
public class StorageException extends ProjectException {
    public StorageException(String mensagem) { super(mensagem, HttpStatus.BAD_GATEWAY); }
}
