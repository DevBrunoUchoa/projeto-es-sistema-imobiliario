package com.campusliving.exception;

import com.campusliving.exception.CustomErrorType;
import com.campusliving.exception.ProjectException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;

@ControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    private CustomErrorType defaultCustomErrorTypeConstruct(String message) {
        return CustomErrorType.builder()
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .message(message)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        CustomErrorType customErrorType = defaultCustomErrorTypeConstruct(
                "Erros de validacao encontrados"
        );
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            customErrorType.getErrors().add(fieldError.getDefaultMessage());
        }
        return customErrorType;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onConstraintViolation(ConstraintViolationException e) {
        CustomErrorType customErrorType = defaultCustomErrorTypeConstruct(
                "Erros de validacao encontrados"
        );
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            customErrorType.getErrors().add(violation.getMessage());
        }
        return customErrorType;
    }

    // Sem @ResponseStatus fixo de propósito: o status agora vem da própria
    // exceção (e.getStatus()), já que subclasses de ProjectException podem
    // representar 404, 403, 409, etc., não só 400.
    @ExceptionHandler(ProjectException.class)
    @ResponseBody
    public ResponseEntity<CustomErrorType> onProjectException(ProjectException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(defaultCustomErrorTypeConstruct(e.getMessage()));
    }

    // Último recurso: qualquer exceção que não seja um dos tipos de negócio
    // acima é mesmo inesperada (bug, falha de infra, etc.) — por isso loga
    // o stack trace inteiro. Exceções de negócio (senha errada, e-mail já
    // cadastrado, ...) devem usar ProjectException/subclasses para cair nos
    // handlers de cima com o status HTTP correto, não vir parar aqui.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public CustomErrorType onUnhandledException(Exception e) {
        log.error("Exceção não tratada", e);
        return defaultCustomErrorTypeConstruct("Ocorreu um erro inesperado. Tente novamente.");
    }

}