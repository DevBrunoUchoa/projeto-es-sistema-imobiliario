package com.campusliving.dto.avaliacao;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.avaliacao.Avaliacao;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("adId")
    private UUID adId;

    // Podem vir null: RNF/LEG-02 anonimiza avaliador/avaliado quando a conta
    // correspondente é excluída (ver comentário em Avaliacao.java).
    @JsonProperty("avaliadorId")
    private UUID avaliadorId;

    @JsonProperty("avaliadoId")
    private UUID avaliadoId;

    @JsonProperty("nota")
    private Short nota;

    @JsonProperty("comentario")
    private String comentario;

    @JsonProperty("respostaLocador")
    private String respostaLocador;

    @JsonProperty("contatoPrevio")
    private boolean contatoPrevio;

    @JsonProperty("dataCriacao")
    private OffsetDateTime dataCriacao;

    public AvaliacaoResponseDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.adId = avaliacao.getAdId();
        this.avaliadorId = avaliacao.getAvaliadorId();
        this.avaliadoId = avaliacao.getAvaliadoId();
        this.nota = avaliacao.getNota();
        this.comentario = avaliacao.getComentario();
        this.respostaLocador = avaliacao.getRespostaLocador();
        this.contatoPrevio = avaliacao.isContatoPrevio();
        this.dataCriacao = avaliacao.getDataCriacao();
    }
}