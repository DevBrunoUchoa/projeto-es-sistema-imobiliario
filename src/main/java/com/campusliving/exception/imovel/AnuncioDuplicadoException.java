package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-12/RF-14: já existe um anúncio ATIVO para este imóvel (índice único
// parcial uq_ads_um_ativo_por_imovel, V9, é a garantia real contra corrida).
public class AnuncioDuplicadoException extends ProjectException {
    public AnuncioDuplicadoException() {
        super("Ja existe um anuncio ativo para este imovel.", HttpStatus.CONFLICT);
    }
}
