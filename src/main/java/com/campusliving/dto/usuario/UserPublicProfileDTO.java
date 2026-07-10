package com.campusliving.dto.usuario;

import java.util.UUID;
import java.math.BigDecimal;

import com.campusliving.model.usuario.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta do GET /usuarios/:id/publico (RF-07, RNF/LEG-03).
 *
 * <p>{@code email}/{@code telefone} só vêm preenchidos quando
 * {@code contatoLiberado} é {@code true} (o próprio dono do perfil, um ADMIN,
 * ou alguém que já registrou interesse em algum anúncio deste locador).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicProfileDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("curso")
    private String curso;

    @JsonProperty("instituicao")
    private String instituicao;

    @JsonProperty("fotoUrl")
    private String fotoUrl;

    @JsonProperty("tipoConta")
    private User.Tipo tipoConta;

    @JsonProperty("verificado")
    private boolean verificado;

    @JsonProperty("contatoLiberado")
    private boolean contatoLiberado;

    // null quando contatoLiberado = false
    @JsonProperty("email")
    private String email;

    @JsonProperty("telefone")
    private String telefone;

    // RF-30/RF-07: reputação do locador, recalculada por trigger (V19).
    // Exposto aqui conforme o RF-07 lista entre os "dados exibidos (leitura)"
    // do perfil público.
    @JsonProperty("notaMedia")
    private BigDecimal notaMedia;

    @JsonProperty("totalAvaliacoes")
    private Integer totalAvaliacoes;

    public static UserPublicProfileDTO of(User usuario, boolean contatoLiberado) {
        return UserPublicProfileDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .bio(usuario.getBio())
                .curso(usuario.getCurso())
                .instituicao(usuario.getInstituicao())
                .fotoUrl(usuario.getFotoUrl())
                .tipoConta(usuario.getTipoConta())
                .verificado(usuario.getVerificado())
                .contatoLiberado(contatoLiberado)
                .email(contatoLiberado ? usuario.getEmail() : null)
                .telefone(contatoLiberado ? usuario.getTelefone() : null)
                .notaMedia(usuario.getNotaMedia())
                .totalAvaliacoes(usuario.getTotalAvaliacoes())
                .build();
    }
}
