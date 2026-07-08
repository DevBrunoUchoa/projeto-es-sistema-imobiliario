package com.campusliving.exception;

import org.springframework.http.HttpStatus;

public class ProjectException extends RuntimeException {

    private final HttpStatus status;

    public ProjectException() {
        super("Erro inesperado no Pits A!");
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ProjectException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    // Construtor usado pelas subclasses que precisam de um HTTP status
    // diferente de 400 (404 quando o recurso não existe, 403 quando falta
    // permissão, 409 em conflitos/duplicidade). Antes disso TODA exceção de
    // negócio virava 400 — inclusive "usuário não encontrado".
    public ProjectException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
