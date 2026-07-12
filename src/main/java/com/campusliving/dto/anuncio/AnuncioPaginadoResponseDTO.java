package com.campusliving.dto.anuncio;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnuncioPaginadoResponseDTO {
    private Long totalItems;
    private Integer totalPages;
    private Integer page;
    private Integer limit;
    private List<AnuncioResponseDTO> items;
}