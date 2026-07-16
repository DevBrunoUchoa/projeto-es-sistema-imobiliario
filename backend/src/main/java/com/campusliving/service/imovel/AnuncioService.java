package com.campusliving.service.imovel;

import com.campusliving.dto.anuncio.AnuncioEstatisticasResponseDTO;
import com.campusliving.dto.anuncio.AnuncioMapaResponseDTO;
import com.campusliving.dto.anuncio.AnuncioPaginadoResponseDTO;
import com.campusliving.dto.anuncio.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.anuncio.AnuncioRequestDTO;
import com.campusliving.dto.anuncio.AnuncioResponseDTO;
import com.campusliving.dto.anuncio.AnuncioUpdateRequestDTO;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnuncioService {

    // Velocidades médias para aproximar o tempo a partir da distância em linha
    // reta (RF-16): ~5 km/h a pé (~83 m/min) e ~18 km/h de ônibus (~300 m/min).
    private static final double METROS_POR_MIN_PE = 83.0;
    private static final double METROS_POR_MIN_ONIBUS = 300.0;

    private final AnuncioRepository anuncioRepository;
    private final ImovelRepository imovelRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ImagemAnuncioService imagemAnuncioService;

    @Value("${app.geocoding.ufcg-lat:-7.21528}")
    private double ufcgLat;

    @Value("${app.geocoding.ufcg-lon:-35.90894}")
    private double ufcgLon;

    @Transactional
    public AnuncioResponseDTO publicarAnuncio(AnuncioRequestDTO request, String email) {
        // 1. Buscar o locador pelo email
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Locador não encontrado");
        }
        User locador = users.get(0);
        UUID locadorId = locador.getId();

        // 2. Verificar se o locador é LOCADOR, MISTO ou ADMIN
        if (!"LOCADOR".equals(locador.getTipoConta().name()) &&
            !"MISTO".equals(locador.getTipoConta().name()) &&
            !"ADMIN".equals(locador.getTipoConta().name())) {
            throw new RuntimeException("Apenas LOCADOR, MISTO ou ADMIN podem publicar anúncios");
        }

        // 3. Buscar o imóvel
        UUID imovelId = UUID.fromString(request.getImovelId());
        Imovel imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        // 4. Verificar se o imóvel pertence ao locador
        if (!imovel.getProprietarioId().equals(locadorId)) {
            throw new RuntimeException("Este imóvel não pertence ao locador informado");
        }

        // 5. Verificar se já existe um anúncio ATIVO para este imóvel
        List<Anuncio> anunciosExistentes = anuncioRepository.findByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO);
        if (!anunciosExistentes.isEmpty()) {
            throw new RuntimeException("Já existe um anúncio ativo para este imóvel");
        }

        // RF-16 (RNF/PER-04): distância até a UFCG pré-computada na publicação,
        // via PostGIS. Sem geometria (endereço não geocodificado) -> fallback.
        Integer distanciaUfcg = anuncioRepository.calcularDistanciaUfcgMetros(imovelId, ufcgLat, ufcgLon);
        boolean geoFallback = (distanciaUfcg == null);
        Integer tempoPeMin = geoFallback ? null : Math.max(1, (int) Math.round(distanciaUfcg / METROS_POR_MIN_PE));
        Integer tempoOnibusMin = geoFallback ? null : Math.max(1, (int) Math.round(distanciaUfcg / METROS_POR_MIN_ONIBUS));

        // 6. Criar o anúncio
        Anuncio anuncio = Anuncio.builder()
                .imovelId(imovelId)
                .locadorId(locadorId)
                .titulo(request.getTitulo())
                .tipoOferta(Anuncio.TipoOferta.valueOf(request.getTipoOferta()))
                .precoAluguel(request.getPrecoAluguel())
                .precoCondominio(request.getPrecoCondominio())
                .precoIptu(request.getPrecoIptu())
                .status(Anuncio.Status.ATIVO)
                .descricao(request.getDescricao())
                .vagasTotal(request.getVagasTotal())
                .vagasDisponiveis(request.getVagasDisponiveis())
                .visualizacoes(0)
                .dataPublicacao(OffsetDateTime.now())
                .destaque(false)
                .distanciaUfcgMetros(distanciaUfcg)
                .tempoPeMin(tempoPeMin)
                .tempoOnibusMin(tempoOnibusMin)
                .geoFallback(geoFallback)
                .build();

        Anuncio savedAnuncio = anuncioRepository.save(anuncio);

        // 7. Registrar log
        auditLogService.registrarAcao(
                locadorId,
                "PUBLICAR_ANUNCIO",
                "Anuncio",
                savedAnuncio.getId()
        );

        return AnuncioResponseDTO.builder()
                .id(savedAnuncio.getId())
                .imovelId(savedAnuncio.getImovelId())
                .locadorId(savedAnuncio.getLocadorId())
                .titulo(savedAnuncio.getTitulo())
                .tipoOferta(savedAnuncio.getTipoOferta().name())
                .precoAluguel(savedAnuncio.getPrecoAluguel())
                .precoCondominio(savedAnuncio.getPrecoCondominio())
                .precoIptu(savedAnuncio.getPrecoIptu())
                .status(savedAnuncio.getStatus().name())
                .descricao(savedAnuncio.getDescricao())
                .vagasTotal(savedAnuncio.getVagasTotal())
                .vagasDisponiveis(savedAnuncio.getVagasDisponiveis())
                .visualizacoes(savedAnuncio.getVisualizacoes())
                .dataPublicacao(savedAnuncio.getDataPublicacao())
                .mensagem("Anúncio publicado com sucesso!")
                .build();
    }

    @Transactional
    public AnuncioResponseDTO atualizarStatus(UUID anuncioId, String novoStatus, String email) {
        // 1. Buscar o locador
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Locador não encontrado");
        }
        User locador = users.get(0);
        UUID locadorId = locador.getId();

        // 2. Buscar o anúncio
        Anuncio anuncio = anuncioRepository.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        // 3. Verificar se o locador é o dono do anúncio (ou ADMIN)
        if (!anuncio.getLocadorId().equals(locadorId) && !"ADMIN".equals(locador.getTipoConta().name())) {
            throw new RuntimeException("Você não tem permissão para alterar este anúncio");
        }

        // 4. Validar o status
        try {
            Anuncio.Status status = Anuncio.Status.valueOf(novoStatus);
            anuncio.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido. Valores permitidos: ATIVO, INATIVO, ALUGADO");
        }

        Anuncio savedAnuncio = anuncioRepository.save(anuncio);

        // 5. Registrar log
        auditLogService.registrarAcao(
                locadorId,
                "ATUALIZAR_STATUS_ANUNCIO",
                "Anuncio",
                savedAnuncio.getId()
        );

        return mapToResponse(savedAnuncio);
    }

    @Transactional(readOnly = true)
    public List<AnuncioResponseDTO> listarMeusAnuncios(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Locador não encontrado");
        }
        UUID locadorId = users.get(0).getId();

        return anuncioRepository.findByLocadorIdOrderByDataPublicacaoDesc(locadorId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AnuncioResponseDTO mapToResponse(Anuncio anuncio) {
    return AnuncioResponseDTO.builder()
            .id(anuncio.getId())
            .imovelId(anuncio.getImovelId())
            .locadorId(anuncio.getLocadorId())
            .titulo(anuncio.getTitulo())
            .tipoOferta(anuncio.getTipoOferta().name())
            .precoAluguel(anuncio.getPrecoAluguel())
            .precoCondominio(anuncio.getPrecoCondominio())
            .precoIptu(anuncio.getPrecoIptu())
            .status(anuncio.getStatus().name())
            .descricao(anuncio.getDescricao())
            .vagasTotal(anuncio.getVagasTotal())
            .vagasDisponiveis(anuncio.getVagasDisponiveis())
            .visualizacoes(anuncio.getVisualizacoes())
            .dataPublicacao(anuncio.getDataPublicacao())
            .build();
    }

    @Transactional
    public AnuncioResponseDTO editarAnuncio(UUID anuncioId, AnuncioUpdateRequestDTO request, String email) {
        // 1. Buscar o locador
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Locador não encontrado");
        }
        User locador = users.get(0);
        UUID locadorId = locador.getId();

        // 2. Buscar o anúncio
        Anuncio anuncio = anuncioRepository.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        // 3. Verificar permissão (dono ou ADMIN)
        if (!anuncio.getLocadorId().equals(locadorId) && !"ADMIN".equals(locador.getTipoConta().name())) {
            throw new RuntimeException("Você não tem permissão para editar este anúncio");
        }

        // 4. Atualizar os campos
        anuncio.setTitulo(request.getTitulo());
        anuncio.setDescricao(request.getDescricao());
        anuncio.setTipoOferta(Anuncio.TipoOferta.valueOf(request.getTipoOferta()));
        anuncio.setPrecoAluguel(request.getPrecoAluguel());
        anuncio.setPrecoCondominio(request.getPrecoCondominio());
        anuncio.setPrecoIptu(request.getPrecoIptu());
        anuncio.setVagasTotal(request.getVagasTotal());
        anuncio.setVagasDisponiveis(request.getVagasDisponiveis());

        Anuncio savedAnuncio = anuncioRepository.save(anuncio);

        // 5. Registrar log
        auditLogService.registrarAcao(
                locadorId,
                "EDITAR_ANUNCIO",
                "Anuncio",
                savedAnuncio.getId()
        );

        return mapToResponse(savedAnuncio);
    }

    @Transactional(readOnly = true)
    public AnuncioDetalhesResponseDTO buscarDetalhes(UUID anuncioId) {
        // 1. Buscar o anúncio
        Anuncio anuncio = anuncioRepository.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        // 2. Buscar o imóvel
        Imovel imovel = imovelRepository.findById(anuncio.getImovelId())
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        // 3. Incrementar visualizações
        anuncio.setVisualizacoes(anuncio.getVisualizacoes() + 1);
        anuncioRepository.save(anuncio);

        // 4. Buscar imagens
        List<String> imagens = imagemAnuncioService.list(anuncioId).stream()
                .map(com.campusliving.dto.imovel.ImagemAnuncioResponseDTO::getUrl)
                .toList();

        // 5. Buscar nota média
        Double notaMedia = null;
        Integer totalAvaliacoes = 0;

        return AnuncioDetalhesResponseDTO.builder()
                // Dados do anúncio
                .id(anuncio.getId())
                .locadorId(anuncio.getLocadorId())
                .titulo(anuncio.getTitulo())
                .tipoOferta(anuncio.getTipoOferta().name())
                .precoAluguel(anuncio.getPrecoAluguel())
                .precoCondominio(anuncio.getPrecoCondominio())
                .precoIptu(anuncio.getPrecoIptu())
                .status(anuncio.getStatus().name())
                .descricao(anuncio.getDescricao())
                .vagasTotal(anuncio.getVagasTotal())
                .vagasDisponiveis(anuncio.getVagasDisponiveis())
                .visualizacoes(anuncio.getVisualizacoes())
                .dataPublicacao(anuncio.getDataPublicacao())
                // Dados do imóvel
                .imovelId(imovel.getId())
                .tipoImovel(imovel.getTipo().name())
                .cep(imovel.getCep())
                .rua(imovel.getRua())
                .numero(imovel.getNumero())
                .complemento(imovel.getComplemento())
                .bairro(imovel.getBairro())
                .cidade(imovel.getCidade())
                .estado(imovel.getEstado())
                .latitude(imovel.getLatitude())
                .longitude(imovel.getLongitude())
                .mobiliado(imovel.isMobiliado())
                .permitePets(imovel.isPermitePets())
                .permiteFumantes(imovel.isPermiteFumantes())
                .incluiAlimentacao(imovel.isIncluiAlimentacao())
                // Distâncias
                .distanciaUfcgMetros(anuncio.getDistanciaUfcgMetros())
                .tempoPeMin(anuncio.getTempoPeMin())
                .tempoOnibusMin(anuncio.getTempoOnibusMin())
                // Imagens e avaliações
                .imagens(imagens)
                .notaMedia(notaMedia)
                .totalAvaliacoes(totalAvaliacoes)
                .build();
    }

    @Transactional(readOnly = true)
    public AnuncioEstatisticasResponseDTO buscarEstatisticas(UUID anuncioId, String email) {
        // 1. Buscar o locador
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Locador não encontrado");
        }
        User locador = users.get(0);

        // 2. Buscar o anúncio
        Anuncio anuncio = anuncioRepository.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        // 3. Verificar permissão (apenas dono ou ADMIN)
        if (!anuncio.getLocadorId().equals(locador.getId()) && !"ADMIN".equals(locador.getTipoConta().name())) {
            throw new RuntimeException("Você não tem permissão para ver as estatísticas deste anúncio");
        }

        // 4. Buscar visualizações agrupadas por dia
        Map<LocalDate, Long> visualizacoesPorDia = buscarVisualizacoesPorDia(anuncioId);

        return AnuncioEstatisticasResponseDTO.builder()
                .totalVisualizacoes(anuncio.getVisualizacoes().longValue())
                .visualizacoesPorDia(visualizacoesPorDia)
                .build();
    }

    private Map<LocalDate, Long> buscarVisualizacoesPorDia(UUID anuncioId) {
        // TODO: Implementar quando tiver uma tabela de histórico de visualizações
        // Por enquanto, retorna um mapa vazio
        return Map.of();
    }

    @Transactional(readOnly = true)
    public List<AnuncioMapaResponseDTO> buscarAnunciosParaMapa() {
        // Buscar todos os anúncios ATIVOS
        List<Anuncio> anuncios = anuncioRepository.findByStatus(Anuncio.Status.ATIVO);

        return anuncios.stream()
                .map(this::mapToMapaResponse)
                .collect(Collectors.toList());
    }

    private AnuncioMapaResponseDTO mapToMapaResponse(Anuncio anuncio) {
        // Buscar o imóvel para obter latitude/longitude
        Imovel imovel = imovelRepository.findById(anuncio.getImovelId())
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        return AnuncioMapaResponseDTO.builder()
                .id(anuncio.getId())
                .latitude(imovel.getLatitude())
                .longitude(imovel.getLongitude())
                .preco(anuncio.getPrecoAluguel())
                .tipo(imovel.getTipo().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AnuncioPaginadoResponseDTO buscarAnunciosPaginados(int page, int limit) {
        // Validar parâmetros
        if (page < 0) page = 0;
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        // Criar Pageable
        Pageable pageable = PageRequest.of(page, limit);

        // Buscar anúncios ATIVOS com paginação
        Page<Anuncio> pageResult = anuncioRepository.findByStatus(Anuncio.Status.ATIVO, pageable);

        // Converter para DTO
        List<AnuncioResponseDTO> items = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return AnuncioPaginadoResponseDTO.builder()
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .page(pageResult.getNumber())
                .limit(pageResult.getSize())
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public AnuncioPaginadoResponseDTO buscarAnunciosPaginadosComSort(
            int page, int limit, String sortBy) {

        if (page < 0) page = 0;
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Anuncio> pageResult = anuncioRepository.findByStatus(Anuncio.Status.ATIVO, pageable);

        List<AnuncioResponseDTO> items = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return AnuncioPaginadoResponseDTO.builder()
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .page(pageResult.getNumber())
                .limit(pageResult.getSize())
                .items(items)
                .build();
    }

    private Sort getSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "dataPublicacao");
        }

        return switch (sortBy) {
            case "preco_asc" -> Sort.by(Sort.Direction.ASC, "precoAluguel");
            case "preco_desc" -> Sort.by(Sort.Direction.DESC, "precoAluguel");
            case "distancia_asc" -> Sort.by(Sort.Direction.ASC, "distanciaUfcgMetros");
            case "created_desc" -> Sort.by(Sort.Direction.DESC, "dataPublicacao");
            default -> Sort.by(Sort.Direction.DESC, "dataPublicacao");
        };
    }

    @Transactional(readOnly = true)
    public AnuncioPaginadoResponseDTO buscarAnunciosComFiltros(
            int page, int limit, String sortBy,
            BigDecimal precoMax,
            Integer distanciaMaxMetros,
            Boolean mobiliado,
            Boolean permitePets,
            Boolean permiteFumantes,
            Boolean incluiAlimentacao,
            String tipoOferta
    ) {
        if (page < 0) page = 0;
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(page, limit, sort);

        // Buscar anúncios com filtros
        Page<Anuncio> pageResult = anuncioRepository.findByFiltros(
                Anuncio.Status.ATIVO,
                precoMax,
                distanciaMaxMetros,
                mobiliado,
                permitePets,
                permiteFumantes,
                incluiAlimentacao,
                tipoOferta,
                pageable
        );

        List<AnuncioResponseDTO> items = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return AnuncioPaginadoResponseDTO.builder()
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .page(pageResult.getNumber())
                .limit(pageResult.getSize())
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public AnuncioPaginadoResponseDTO buscarAnunciosComTexto(
            int page, int limit, String sortBy, String query,
            BigDecimal precoMax,
            Integer distanciaMaxMetros,
            Boolean mobiliado,
            Boolean permitePets,
            Boolean permiteFumantes,
            Boolean incluiAlimentacao,
            String tipoOferta
    ) {
        if (page < 0) page = 0;
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Anuncio> pageResult;

        // Se tiver query textual, usa busca com texto
        if (query != null && !query.trim().isEmpty()) {
            pageResult = anuncioRepository.buscarPorTexto(
                    Anuncio.Status.ATIVO,
                    query.trim(),
                    pageable
            );
        } else {
            // Senão, usa os filtros normais
            pageResult = anuncioRepository.findByFiltros(
                    Anuncio.Status.ATIVO,
                    precoMax,
                    distanciaMaxMetros,
                    mobiliado,
                    permitePets,
                    permiteFumantes,
                    incluiAlimentacao,
                    tipoOferta,
                    pageable
            );
        }

        List<AnuncioResponseDTO> items = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return AnuncioPaginadoResponseDTO.builder()
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .page(pageResult.getNumber())
                .limit(pageResult.getSize())
                .items(items)
                .build();
    }
}