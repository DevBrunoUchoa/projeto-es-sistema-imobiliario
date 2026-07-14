package com.campusliving.dto.usuario;

import java.util.UUID;

import com.campusliving.model.usuario.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    // Repare que aqui NÃO tem @Id/@GeneratedValue: isso é um DTO de saída,
    // não uma entidade JPA — essas anotações nunca deveriam ter estado aqui
    // (não tinham efeito nenhum, e um id UUID gerado no banco não é "gerado"
    // no DTO de resposta).
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("email")
    @NotBlank(message = "")
    private String email;

    @JsonProperty("nome")
    @NotBlank(message = "")
    private String nome;

    @JsonProperty("telefone")
    @NotBlank(message = "")
    private String telefone;

    @JsonProperty("bio")
    @NotBlank(message = "")
    private String bio;

    // RF-06: expostos aqui para o cliente conseguir ler o que acabou de
    // atualizar via PUT /usuarios/:id (ver UserServiceImpl#atualizarPerfil).
    @JsonProperty("curso")
    private String curso;

    @JsonProperty("instituicao")
    private String instituicao;

    @JsonProperty("verificado")
    @NotBlank(message = "")
    private boolean verificado;

    @JsonProperty("ativo")
    @NotBlank(message = "")
    private boolean ativo;


    public UserResponseDTO(User usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.bio = usuario.getBio();
        this.curso = usuario.getCurso();
        this.instituicao = usuario.getInstituicao();
        this.verificado = usuario.getVerificado();
        this.ativo = usuario.getAtivo();
    }

}
