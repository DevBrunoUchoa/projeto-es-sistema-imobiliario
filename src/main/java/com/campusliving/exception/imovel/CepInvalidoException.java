package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-11: CEP mal formado, inexistente, ou fora de Campina Grande-PB.
// 422 (não 400): o corpo da requisição está sintaticamente correto, o
// problema é semântico (o valor não passa numa regra de negócio).
public class CepInvalidoException extends ProjectException {
    public CepInvalidoException(String motivo) {
        super("CEP invalido: " + motivo, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
