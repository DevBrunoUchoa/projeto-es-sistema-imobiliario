package com.campusliving.exception;

public class ProjectException extends RuntimeException {
    public ProjectException() {
        super("Erro inesperado no Pits A!");
    }

    public ProjectException(String message) {
        super(message);
    }
}
