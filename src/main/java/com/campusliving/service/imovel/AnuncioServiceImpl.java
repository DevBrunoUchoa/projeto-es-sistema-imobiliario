package com.campusliving.service.imovel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.dto.imovel.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.imovel.AnuncioRequestDTO;
import com.campusliving.dto.imovel.AnuncioResponseDTO;
import com.campusliving.dto.imovel.AnuncioStatusUpdateDTO;
import com.campusliving.dto.imovel.AnuncioUpdateRequestDTO;
import com.campusliving.dto.imovel.RegrasCasaRequestDTO;
import com.campusliving.dto.imovel.VisualizacaoPorDiaDTO;
import com.campusliving.exception.imovel.AnuncioDuplicadoException;
import com.campusliving.exception.imovel.AnuncioNaoEncontradoException;
import com.campusliving.exception.imovel.ImovelNaoEncontradoException;
import com.campusliving.exception.imovel.StatusAnuncioInvalidoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.imovel.ImagemAnuncio;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.imovel.RegrasCasa;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.avaliacao.AvaliacaoRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImagemAnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.imovel.RegrasCasaRepository;
import com.campusliving.repository.imovel.VisualizacaoAnuncioRepository;
import com.campusliving.repository.usuario.UserRepository;

@Service
public class AnuncioServiceImpl implements AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final ImovelRepository imovelRepository;
    private final RegrasCasaRepository regrasCasaRepository;
    private final ImagemAnuncioRepository imagemAnuncioRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final VisualizacaoAnuncioRepository visualizacaoAnuncioRepository;
    private final UserRepository userRepository;
    private final AnuncioGeoService anuncioGeoService;

    public AnuncioServiceImpl(
            AnuncioRepository anuncioRepository,
            ImovelRepository imovelRepository,
            RegrasCasaRepository regrasCasaRepository,
            ImagemAnuncioRepository imagemAnuncioRepository,
            AvaliacaoRepository avaliacaoRepository,
            VisualizacaoAnuncioRepository visualizacaoAnuncioRepository,
            UserRepository userRepository,
            AnuncioGeoService anuncioGeoService
    ) {
        this.anuncioRepository = anuncioRepository;
        this.imovelRepository = imovelRepository;
        this.regrasCasaRepository = regrasCasaRepository;
        this.imagemAnuncioRepository = imagemAnuncioRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.visualizacaoAnuncioRepository = visualizacaoAnuncioRepository;
        this.userRepository = userRepository;
        this.anuncioGeoService = anuncioGeoService;
    }

    @Override
    public AnuncioResponseDTO publicar(AnuncioRequestDTO dto, UUID requesterId) {
        Imovel imovel = imovelRepository.findById(dto.getImovelId()).orElseThrow(ImovelNaoEncontradoException::new);
        // RF-12: só o dono do imóvel publica anúncio para ele.
        if (requesterId == null || !requesterId.equals(imovel.getProprietarioId())) {
            throw new AcessoNegadoException();
        }
        if (anuncioRepository.existsByImovelIdAndStatus(dto.getImovelId(), Anuncio.Status.ATIVO)) {
            throw new AnuncioDuplicadoException();
        }

        Integer vagasTotal = dto.getVagasTotal() != null ? dto.getVagasTotal() : 1;
        Anuncio anuncio = Anuncio.builder()
                .imovelId(imovel.getId())
                .locadorId(requesterId)
                .titulo(dto.getTitulo())
                .tipoOferta(dto.getTipoOferta())
                .precoAluguel(dto.getPrecoAluguel())
                .precoCondominio(dto.getPrecoCondominio() != null ? dto.getPrecoCondominio() : BigDecimal.ZERO)
                .precoIptu(dto.getPrecoIptu() != null ? dto.getPrecoIptu() : BigDecimal.ZERO)
                .status(Anuncio.Status.ATIVO)
                .geoFallback(false)
                .descricao(dto.getDescricao())
                .vagasTotal(vagasTotal)
                .vagasDisponiveis(vagasTotal)
                .destaque(false)
                .visualizacoes(0)
                .dataPublicacao(OffsetDateTime.now())
                .build();

        try {
            anuncioRepository.save(anuncio);
        } catch (DataIntegrityViolationException e) {
            // Corrida rara: ver mesmo padrão em RoommateServiceImpl#solicitarMatch.
            // A rede de segurança real é o índice único parcial
            // uq_ads_um_ativo_por_imovel (V9).
            throw new AnuncioDuplicadoException();
        }

        if (dto.getRegrasCasa() != null) {
            salvarRegrasCasa(anuncio.getId(), dto.getRegrasCasa(), null);
        }

        anuncioGeoService.calcularDistanciaUfcg(anuncio.getId());

        return new AnuncioResponseDTO(anuncio);
    }

    @Override
    public AnuncioResponseDTO atualizar(UUID anuncioId, AnuncioUpdateRequestDTO dto, UUID requesterId) {
        Anuncio anuncio = anuncioRepository.findById(anuncioId).orElseThrow(AnuncioNaoEncontradoException::new);
        exigirDonoOuAdmin(requesterId, anuncio.getLocadorId());

        if (dto.getTitulo() != null) {
            anuncio.setTitulo(dto.getTitulo());
        }
        if (dto.getPrecoAluguel() != null) {
            anuncio.setPrecoAluguel(dto.getPrecoAluguel());
        }
        if (dto.getPrecoCondominio() != null) {
            anuncio.setPrecoCondominio(dto.getPrecoCondominio());
        }
        if (dto.getPrecoIptu() != null) {
            anuncio.setPrecoIptu(dto.getPrecoIptu());
        }
        if (dto.getDescricao() != null) {
            anuncio.setDescricao(dto.getDescricao());
        }
        anuncioRepository.save(anuncio);

        if (dto.getAceitaFumantes() != null || dto.getPetFriendly() != null || dto.getAlimentacaoInclusa() != null) {
            RegrasCasaRequestDTO regrasDto = RegrasCasaRequestDTO.builder()
                    .aceitaFumantes(dto.getAceitaFumantes())
                    .petFriendly(dto.getPetFriendly())
                    .alimentacaoInclusa(dto.getAlimentacaoInclusa())
                    .build();
            salvarRegrasCasa(anuncioId, regrasDto, regrasCasaRepository.findById(anuncioId).orElse(null));
        }

        return new AnuncioResponseDTO(anuncio);
    }

    @Override
    public AnuncioResponseDTO atualizarStatus(UUID anuncioId, AnuncioStatusUpdateDTO dto, UUID requesterId) {
        Anuncio anuncio = anuncioRepository.findById(anuncioId).orElseThrow(AnuncioNaoEncontradoException::new);
        exigirDonoOuAdmin(requesterId, anuncio.getLocadorId());

        Anuncio.Status novoStatus = parseStatus(dto.getStatus());

        // Reativar: mesma checagem de duplicidade do POST /anuncios, agora
        // ignorando o próprio anúncio.
        if (novoStatus == Anuncio.Status.ATIVO && anuncio.getStatus() != Anuncio.Status.ATIVO
                && anuncioRepository.existsByImovelIdAndStatusAndIdNot(anuncio.getImovelId(), Anuncio.Status.ATIVO, anuncioId)) {
            throw new AnuncioDuplicadoException();
        }

        anuncio.setStatus(novoStatus);
        try {
            anuncioRepository.save(anuncio);
        } catch (DataIntegrityViolationException e) {
            throw new AnuncioDuplicadoException();
        }

        return new AnuncioResponseDTO(anuncio);
    }

    @Override
    @Transactional
    public AnuncioDetalhesResponseDTO getDetalhes(UUID anuncioId, UUID requesterId) {
        Anuncio anuncio = anuncioRepository.findById(anuncioId).orElseThrow(AnuncioNaoEncontradoException::new);

        // RF-17: contagem de visualizações não conta o próprio dono
        // navegando no anúncio (senão a métrica ficaria inflada só pelo
        // locador conferindo o próprio anúncio).
        if (requesterId == null || !requesterId.equals(anuncio.getLocadorId())) {
            visualizacaoAnuncioRepository.registrarVisualizacao(anuncioId);
            anuncio.setVisualizacoes(anuncio.getVisualizacoes() + 1);
            anuncioRepository.save(anuncio);
        }

        List<String> imagensUrls = imagemAnuncioRepository.findByAdIdOrderByOrdemAsc(anuncioId).stream()
                .map(ImagemAnuncio::getUrl)
                .collect(Collectors.toList());

        Double notaMedia = avaliacaoRepository.calcularNotaMedia(anuncioId);

        return new AnuncioDetalhesResponseDTO(anuncio, imagensUrls, notaMedia);
    }

    @Override
    public List<VisualizacaoPorDiaDTO> getEstatisticas(UUID anuncioId, UUID requesterId) {
        Anuncio anuncio = anuncioRepository.findById(anuncioId).orElseThrow(AnuncioNaoEncontradoException::new);
        // RF-17: "exclusivo para o dono" — mantemos ADMIN também, pelo mesmo
        // padrão de exigirDonoOuAdmin usado no resto do projeto.
        exigirDonoOuAdmin(requesterId, anuncio.getLocadorId());

        return visualizacaoAnuncioRepository.findByAdIdOrderByDataVisualizacaoAsc(anuncioId).stream()
                .map(v -> VisualizacaoPorDiaDTO.builder()
                        .data(v.getDataVisualizacao())
                        .quantidade(v.getQuantidade())
                        .build())
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private void salvarRegrasCasa(UUID anuncioId, RegrasCasaRequestDTO dto, RegrasCasa existente) {
        RegrasCasa regras = existente != null ? existente : RegrasCasa.builder()
                .adId(anuncioId)
                .restricaoGenero(RegrasCasa.RestricaoGenero.SEM_RESTRICAO)
                .alimentacaoInclusa(RegrasCasa.TipoAlimentacao.NENHUMA)
                .permiteVisitas(true)
                .build();

        if (dto.getAceitaFumantes() != null) {
            regras.setAceitaFumantes(dto.getAceitaFumantes());
        }
        if (dto.getPetFriendly() != null) {
            regras.setPetFriendly(dto.getPetFriendly());
        }
        if (dto.getRestricaoGenero() != null) {
            regras.setRestricaoGenero(dto.getRestricaoGenero());
        }
        if (dto.getNivelBarulho() != null) {
            regras.setNivelBarulho(dto.getNivelBarulho());
        }
        if (dto.getAlimentacaoInclusa() != null) {
            regras.setAlimentacaoInclusa(dto.getAlimentacaoInclusa());
        }
        if (dto.getPermiteVisitas() != null) {
            regras.setPermiteVisitas(dto.getPermiteVisitas());
        }
        if (dto.getHorarioSilencio() != null) {
            regras.setHorarioSilencio(dto.getHorarioSilencio());
        }

        regrasCasaRepository.save(regras);
    }

    private Anuncio.Status parseStatus(String statusBruto) {
        Anuncio.Status status;
        try {
            status = Anuncio.Status.valueOf(statusBruto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StatusAnuncioInvalidoException("use ATIVO, INATIVO ou SUSPENSO");
        }
        // ALUGADO é uma transição de outro fluxo de negócio (fora do escopo
        // deste PATCH — ver comentário na migration V20 e na exceção).
        if (status != Anuncio.Status.ATIVO && status != Anuncio.Status.INATIVO && status != Anuncio.Status.SUSPENSO) {
            throw new StatusAnuncioInvalidoException("use ATIVO, INATIVO ou SUSPENSO");
        }
        return status;
    }

    // Mesmo padrão duplicado em UserServiceImpl/RoommateServiceImpl — ver
    // comentário lá sobre a troca futura por um bean compartilhado (T5.3).
    private void exigirDonoOuAdmin(UUID requesterId, UUID targetId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        if (requesterId.equals(targetId)) {
            return;
        }
        User requester = userRepository.findById(requesterId).orElseThrow(AcessoNegadoException::new);
        if (requester.getTipoConta() != User.Tipo.ADMIN) {
            throw new AcessoNegadoException();
        }
    }
}
