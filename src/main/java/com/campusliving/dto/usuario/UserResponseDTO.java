package com.campusliving.dto.usuario;

import com.campusliving.model.usuario.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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


    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

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
        this.verificado = usuario.getVerificado();
        this.ativo = usuario.getAtivo();
    }

}
