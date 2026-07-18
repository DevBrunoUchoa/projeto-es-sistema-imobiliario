package com.campusliving.exception;

import com.campusliving.exception.CustomErrorType;
import com.campusliving.exception.ProjectException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public CustomErrorType onUnhandledException(Exception e) {
        return defaultCustomErrorTypeConstruct("Ocorreu um erro inesperado. Tente novamente.");
    }

}