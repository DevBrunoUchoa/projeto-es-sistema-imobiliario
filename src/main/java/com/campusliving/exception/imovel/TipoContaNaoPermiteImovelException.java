package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-11: só contas LOCADOR ou MISTO podem cadastrar imóveis (ESTUDANTE
// "puro" ainda não fez a promoção do T5.4.4).
public class TipoContaNaoPermiteImovelException extends ProjectException {
    public TipoContaNaoPermiteImovelException() {
        super("Somente contas LOCADOR ou MISTO podem cadastrar imoveis.", HttpStatus.CONFLICT);
    }
}
