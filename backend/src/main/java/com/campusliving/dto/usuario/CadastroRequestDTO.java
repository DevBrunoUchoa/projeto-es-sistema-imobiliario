package com.campusliving.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CadastroRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotNull(message = "Aceite do LGPD é obrigatório")
    private Boolean aceiteLgpd;

    // ESTUDANTE, LOCADOR ou MISTO (opcional, default ESTUDANTE). ADMIN nunca é
    // aceito aqui — AuthService.cadastrar rejeita explicitamente, já que este
    // endpoint é público (permitAll).
    private String role;
}