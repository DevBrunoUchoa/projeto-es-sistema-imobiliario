package com.campusliving.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.campusliving.model.usuario.User;

/**
 * Corpo do PUT /usuarios/:id (RF-06).
 *
 * <p>Diferente de {@link UserPostPutRequestDTO} (usado no cadastro), aqui os
 * campos são todos opcionais: é uma atualização parcial de perfil, não uma
 * substituição completa do recurso — só os campos não-nulos enviados são
 * alterados (ver UserServiceImpl#atualizarPerfil).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {

    @JsonProperty("nome")
    @Size(max = 150, message = "Nome deve ter no maximo 150 caracteres")
    private String nome;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("telefone")
    @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres")
    private String telefone;

    @JsonProperty("curso")
    @Size(max = 150, message = "Curso deve ter no maximo 150 caracteres")
    private String curso;

    @JsonProperty("instituicao")
    @Size(max = 150, message = "Instituicao deve ter no maximo 150 caracteres")
    private String instituicao;

    @JsonProperty("role")
    private User.Tipo role;
}
