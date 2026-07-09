package com.campusliving.exception.imovel;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// RF-16 (indiretamente RF-11): não foi possível resolver coordenadas para o
// endereço informado (serviço de geocodificação fora do ar, ou endereço não
// encontrado). properties.latitude/longitude são NOT NULL, então sem
// coordenadas o imóvel simplesmente não pode ser criado.
public class EnderecoNaoGeocodificavelException extends ProjectException {
    public EnderecoNaoGeocodificavelException(String motivo) {
        super("Nao foi possivel geocodificar o endereco: " + motivo, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
