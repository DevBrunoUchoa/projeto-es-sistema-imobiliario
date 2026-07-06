package com.campusliving.dto.usuario;

import com.campusliving.model.usuario.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPostPutRequestDTO {

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
    @NotNull(message = "")
    private boolean verificado;   
 
    @JsonProperty("ativo")
    @NotNull(message = "")
    private boolean ativo;

    @JsonProperty("senhaHash")
    @NotBlank(message = "Senha é obrigatória")
    private String senhaHash;

    @JsonProperty("tipoConta")
    @NotNull(message = "Tipo de conta é obrigatório")
    private User.Tipo tipoConta;
}
