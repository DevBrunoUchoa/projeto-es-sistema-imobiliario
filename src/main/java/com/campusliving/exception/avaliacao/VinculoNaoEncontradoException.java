package com.campusliving.exception.avaliacao;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-29 ("avaliador deve ter tido vínculo com o anunciante"): o "vínculo" é
// verificado reaproveitando ContatoRepository#existeContatoEntre — a mesma
// checagem que a RNF/LEG-03 já usa para liberar os dados de contato do
// locador. Não é um booleano que o cliente envia no corpo da requisição (ver
// AvaliacaoServiceImpl), porque isso seria trivialmente falsificável.
public class VinculoNaoEncontradoException extends ProjectException {
    public VinculoNaoEncontradoException() {
        super("E necessario ter registrado interesse (contato previo) com o locador para avaliar.", HttpStatus.FORBIDDEN);
    }
}