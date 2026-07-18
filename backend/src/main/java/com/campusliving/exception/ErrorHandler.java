package com.campusliving.exception;

import com.campusliving.exception.CustomErrorType;
import com.campusliving.exception.ProjectException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    // @PreAuthorize negado lança isso via AOP, na própria invocação do método
    // do controller — chega aqui, não no ExceptionTranslationFilter do Spring
    // Security (que só vê exceções da cadeia de filtros). Sem esse handler,
    // TODO endpoint com @PreAuthorize devolvia 500 pra quem não tinha
    // permissão, em vez de 403 (achado testando o painel admin).
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public CustomErrorType onAuthorizationDenied(AuthorizationDeniedException e) {
        return defaultCustomErrorTypeConstruct("Você não tem permissão para acessar este recurso");
    }

    // Parâmetro obrigatório faltando (?adId=... etc.) e verbo HTTP errado pra
    // rota também são erros do cliente que o Spring já sabe classificar
    // corretamente — sem esses handlers, ambos caíam no catch-all como 500.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onMissingParameter(MissingServletRequestParameterException e) {
        return defaultCustomErrorTypeConstruct("Parâmetro obrigatório ausente: " + e.getParameterName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public CustomErrorType onMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return defaultCustomErrorTypeConstruct("Método HTTP não suportado para esta rota");
    }

    // Rota inexistente dentro de um prefixo permitAll (ex.: /auth/rota-que-nao-
    // existe) chega até o DispatcherServlet sem passar pelo filtro de
    // autenticação — e sem handler dedicado virava 500 em vez de 404. Fora dos
    // prefixos permitAll o comportamento já é outro (401 do authenticationEntryPoint
    // do SecurityConfig, que age antes disso).
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onNoResourceFound(NoResourceFoundException e) {
        return defaultCustomErrorTypeConstruct("Rota não encontrada");
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