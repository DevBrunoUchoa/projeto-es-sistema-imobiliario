package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-14: valor de status fora do conjunto aceito pelo PATCH (ATIVO/INATIVO/
// SUSPENSO — ALUGADO é uma transição de outro fluxo, não deste endpoint).
public class StatusAnuncioInvalidoException extends ProjectException {
    public StatusAnuncioInvalidoException(String motivo) {
        super("Status de anuncio invalido: " + motivo, HttpStatus.BAD_REQUEST);
    }
}
