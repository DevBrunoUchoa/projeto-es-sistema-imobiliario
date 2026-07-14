package com.campusliving.exception.usuario;
import com.campusliving.exception.ProjectException;
import org.springframework.http.HttpStatus;

// Usada quando quem faz a requisição não é nem o dono do recurso nem ADMIN
// (RF-06 e afins: "apenas o próprio usuário ou ADMIN"). Também cobre o caso
// de nenhum requerente ser informado (ver comentário no X-User-Id no
// UserController).
public class AcessoNegadoException extends ProjectException {
    public AcessoNegadoException() {
        super("Voce nao tem permissao para realizar esta acao.", HttpStatus.FORBIDDEN);
    }
}
