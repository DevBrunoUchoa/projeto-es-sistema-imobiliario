package com.campusliving.service.roommate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.campusliving.dto.roommate.PerfilRoommateRequestDTO;
import com.campusliving.dto.roommate.PerfilRoommateResponseDTO;
import com.campusliving.dto.roommate.PreferenciasRoommateRequestDTO;
import com.campusliving.dto.roommate.RoommateCompativelDTO;
import com.campusliving.dto.roommate.RoommateMatchRequestDTO;
import com.campusliving.dto.roommate.RoommateMatchResponseDTO;
import com.campusliving.dto.roommate.RoommateMatchStatusUpdateDTO;
import com.campusliving.exception.roommate.AutoMatchException;
import com.campusliving.exception.roommate.MatchDuplicadoException;
import com.campusliving.exception.roommate.MatchNaoEncontradoException;
import com.campusliving.exception.roommate.PerfilRoommateNaoEncontradoException;
import com.campusliving.exception.roommate.StatusMatchInvalidoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.notificacao.Notificacao;
import com.campusliving.model.roommate.PerfilRoommate;
import com.campusliving.model.roommate.RoommateMatch;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.notificacao.NotificacaoRepository;
import com.campusliving.repository.roommate.PerfilRoommateRepository;
import com.campusliving.repository.roommate.RoommateMatchRepository;
import com.campusliving.repository.usuario.UserRepository;

@Service
public class RoommateServiceImpl implements RoommateService {

    // Limiares do algoritmo de compatibilidade (RF-33). Não vieram
    // especificados no RF em número exato — "próximo"/"sobreposição" são
    // definidos aqui de forma explícita e documentada para não ficar mágico.
    private static final BigDecimal TOLERANCIA_ORCAMENTO_PERCENTUAL = new BigDecimal("0.20"); // 20%
    private static final long TOLERANCIA_DATA_DIAS = 30;

    private final PerfilRoommateRepository perfilRoommateRepository;
    private final RoommateMatchRepository roommateMatchRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final UserRepository userRepository;

    public RoommateServiceImpl(
            PerfilRoommateRepository perfilRoommateRepository,
            RoommateMatchRepository roommateMatchRepository,
            NotificacaoRepository notificacaoRepository,
            UserRepository userRepository
    ) {
        this.perfilRoommateRepository = perfilRoommateRepository;
        this.roommateMatchRepository = roommateMatchRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PerfilRoommateResponseDTO salvarPreferencias(UUID userId, PreferenciasRoommateRequestDTO dto, UUID requesterId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
        exigirDonoOuAdmin(requesterId, userId);

        PerfilRoommate perfil = perfilRoommateRepository.findByUserId(userId)
                .orElseGet(() -> PerfilRoommate.builder()
                        .userId(userId)
                        .aceitaPets(false)
                        .fumante(false)
                        .ativo(false)
                        .jaPossuiCasa(false)
                        .perfilVisivel(false)
                        .build());

        // Atualização parcial: só sobrescreve o que veio preenchido.
        if (dto.getHorarioDorme() != null) {
            perfil.setHorarioDorme(dto.getHorarioDorme());
        }
        if (dto.getHorarioAcorda() != null) {
            perfil.setHorarioAcorda(dto.getHorarioAcorda());
        }
        if (dto.getNivelBarulho() != null) {
            perfil.setNivelBarulhoPreferido(dto.getNivelBarulho());
        }
        if (dto.getFumante() != null) {
            perfil.setFumante(dto.getFumante());
        }
        if (dto.getAceitaPets() != null) {
            perfil.setAceitaPets(dto.getAceitaPets());
        }

        perfilRoommateRepository.save(perfil);
        return new PerfilRoommateResponseDTO(perfil);
    }

    @Override
    public PerfilRoommateResponseDTO ativarPerfil(PerfilRoommateRequestDTO dto, UUID requesterId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        if (!userRepository.existsById(requesterId)) {
            throw new UserNotFoundException();
        }

        PerfilRoommate perfil = perfilRoommateRepository.findByUserId(requesterId)
                .orElseGet(() -> PerfilRoommate.builder()
                        .userId(requesterId)
                        .aceitaPets(false)
                        .fumante(false)
                        .build());

        if (dto.getDescricao() != null) {
            perfil.setDescricao(dto.getDescricao());
        }
        if (dto.getOrcamentoMax() != null) {
            perfil.setOrcamentoMax(dto.getOrcamentoMax());
        }
        if (dto.getDataEntradaDesejada() != null) {
            perfil.setDataEntradaDesejada(dto.getDataEntradaDesejada());
        }
        if (dto.getPeriodoMinMeses() != null) {
            perfil.setPeriodoMinMeses(dto.getPeriodoMinMeses());
        }
        // RF-32: "ativar" — diferente das preferências (T5.8.1), este
        // endpoint sempre grava esses três campos, já que é literalmente o
        // que ele existe para fazer.
        perfil.setJaPossuiCasa(dto.isJaPossuiCasa());
        perfil.setPerfilVisivel(dto.isPerfilVisivel());
        perfil.setAtivo(true);

        perfilRoommateRepository.save(perfil);
        return new PerfilRoommateResponseDTO(perfil);
    }

    @Override
    public PerfilRoommateResponseDTO buscarMeuPerfil(UUID requesterId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        // Sem perfil ainda: devolve um default não-persistido (id null, tudo
        // inativo) em vez de 404, pra o front conseguir renderizar a aba
        // "Meu perfil" em branco sem precisar tratar erro como caso normal.
        PerfilRoommate perfil = perfilRoommateRepository.findByUserId(requesterId)
                .orElseGet(() -> PerfilRoommate.builder()
                        .userId(requesterId)
                        .aceitaPets(false)
                        .fumante(false)
                        .ativo(false)
                        .jaPossuiCasa(false)
                        .perfilVisivel(false)
                        .build());
        return new PerfilRoommateResponseDTO(perfil);
    }

    @Override
    public List<RoommateMatchResponseDTO> listarSolicitacoesPendentes(UUID requesterId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        return roommateMatchRepository
                .findByDestinatarioIdAndStatusOrderByDataSolicitacaoDesc(requesterId, RoommateMatch.Status.PENDENTE)
                .stream()
                .map(RoommateMatchResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoommateCompativelDTO> listarCompativeis(UUID requesterId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        PerfilRoommate meuPerfil = perfilRoommateRepository.findByUserId(requesterId)
                .orElseThrow(PerfilRoommateNaoEncontradoException::new);

        List<PerfilRoommate> candidatos = perfilRoommateRepository
                .findByAtivoTrueAndPerfilVisivelTrueAndUserIdNot(requesterId);

        return candidatos.stream()
                .map(candidato -> {
                    User usuario = userRepository.findById(candidato.getUserId()).orElse(null);
                    return RoommateCompativelDTO.builder()
                            .userId(candidato.getUserId())
                            .nome(usuario != null ? usuario.getNome() : null)
                            .descricao(candidato.getDescricao())
                            .orcamentoMax(candidato.getOrcamentoMax())
                            .nivelBarulhoPreferido(candidato.getNivelBarulhoPreferido())
                            .jaPossuiCasa(candidato.isJaPossuiCasa())
                            .scoreCompatibilidade(calcularCompatibilidade(meuPerfil, candidato))
                            .build();
                })
                .sorted(Comparator.comparingInt(RoommateCompativelDTO::getScoreCompatibilidade).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public RoommateMatchResponseDTO solicitarMatch(RoommateMatchRequestDTO dto, UUID requesterId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        if (requesterId.equals(dto.getDestinatarioId())) {
            throw new AutoMatchException();
        }
        if (!userRepository.existsById(requesterId) || !userRepository.existsById(dto.getDestinatarioId())) {
            throw new UserNotFoundException();
        }

        boolean jaExisteAtivo = roommateMatchRepository
                .findByParEmQualquerSentido(requesterId, dto.getDestinatarioId())
                .stream()
                .anyMatch(m -> m.getStatus() == RoommateMatch.Status.PENDENTE
                        || m.getStatus() == RoommateMatch.Status.ACEITO);
        if (jaExisteAtivo) {
            throw new MatchDuplicadoException();
        }

        RoommateMatch match = RoommateMatch.builder()
                .solicitanteId(requesterId)
                .destinatarioId(dto.getDestinatarioId())
                .status(RoommateMatch.Status.PENDENTE)
                .mensagemInicial(dto.getMensagemInicial())
                .build();
        try {
            roommateMatchRepository.save(match);
        } catch (DataIntegrityViolationException e) {
            // Corrida rara: duas requisições simultâneas passam pela checagem
            // acima antes de qualquer uma salvar. A UNIQUE do banco
            // (uq_roommate_match_par) garante a integridade nesse caso.
            throw new MatchDuplicadoException();
        }

        // RF-35: notifica o destinatário sobre a nova solicitação de conexão.
        notificacaoRepository.save(Notificacao.builder()
                .userId(dto.getDestinatarioId())
                .tipo(Notificacao.Tipo.MATCH)
                .titulo("Nova solicitação de conexão")
                .mensagem("Você recebeu uma nova solicitação de roommate.")
                .lida(false)
                .build());

        return new RoommateMatchResponseDTO(match);
    }

    @Override
    public RoommateMatchResponseDTO responderMatch(UUID matchId, RoommateMatchStatusUpdateDTO dto, UUID requesterId) {
        RoommateMatch match = roommateMatchRepository.findById(matchId)
                .orElseThrow(MatchNaoEncontradoException::new);

        // Só o destinatário original responde — nem o solicitante, nem um
        // terceiro (nem sequer ADMIN: aceitar/recusar é uma decisão pessoal).
        if (requesterId == null || !requesterId.equals(match.getDestinatarioId())) {
            throw new AcessoNegadoException();
        }
        if (match.getStatus() != RoommateMatch.Status.PENDENTE) {
            throw new StatusMatchInvalidoException(
                    "esta solicitacao ja foi respondida (status atual: " + match.getStatus() + ")");
        }

        RoommateMatch.Status novoStatus = parseStatusResposta(dto.getStatus());

        match.setStatus(novoStatus);
        roommateMatchRepository.save(match);

        notificacaoRepository.save(criarNotificacaoResposta(match, novoStatus));

        return new RoommateMatchResponseDTO(match);
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private RoommateMatch.Status parseStatusResposta(String statusBruto) {
        RoommateMatch.Status status;
        try {
            status = RoommateMatch.Status.valueOf(statusBruto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StatusMatchInvalidoException("use ACEITO ou RECUSADO");
        }
        if (status != RoommateMatch.Status.ACEITO && status != RoommateMatch.Status.RECUSADO) {
            throw new StatusMatchInvalidoException("use ACEITO ou RECUSADO");
        }
        return status;
    }

    private Notificacao criarNotificacaoResposta(RoommateMatch match, RoommateMatch.Status novoStatus) {
        boolean aceito = novoStatus == RoommateMatch.Status.ACEITO;
        return Notificacao.builder()
                .userId(match.getSolicitanteId())
                .tipo(Notificacao.Tipo.MATCH)
                .titulo(aceito ? "Seu match foi aceito!" : "Seu match foi recusado")
                .mensagem(aceito
                        ? "A pessoa aceitou sua solicitacao de roommate."
                        : "A pessoa recusou sua solicitacao de roommate.")
                .lida(false)
                .build();
    }

    /**
     * RF-33: % de critérios em comum entre dois perfis. Cada critério só
     * entra na conta quando AMBOS os perfis têm o dado preenchido (evita
     * penalizar perfis incompletos injustamente); fumante/pets sempre entram
     * porque são booleans com valor padrão sempre definido.
     */
    private int calcularCompatibilidade(PerfilRoommate a, PerfilRoommate b) {
        int criteriosAplicaveis = 0;
        int criteriosCompativeis = 0;

        if (a.getOrcamentoMax() != null && b.getOrcamentoMax() != null) {
            criteriosAplicaveis++;
            if (orcamentosProximos(a.getOrcamentoMax(), b.getOrcamentoMax())) {
                criteriosCompativeis++;
            }
        }

        if (a.getNivelBarulhoPreferido() != null && b.getNivelBarulhoPreferido() != null) {
            criteriosAplicaveis++;
            if (a.getNivelBarulhoPreferido() == b.getNivelBarulhoPreferido()) {
                criteriosCompativeis++;
            }
        }

        criteriosAplicaveis++;
        if (a.isFumante() == b.isFumante()) {
            criteriosCompativeis++;
        }

        criteriosAplicaveis++;
        if (a.isAceitaPets() == b.isAceitaPets()) {
            criteriosCompativeis++;
        }

        if (a.getDataEntradaDesejada() != null && b.getDataEntradaDesejada() != null) {
            criteriosAplicaveis++;
            long diasDiferenca = Math.abs(ChronoUnit.DAYS.between(a.getDataEntradaDesejada(), b.getDataEntradaDesejada()));
            if (diasDiferenca <= TOLERANCIA_DATA_DIAS) {
                criteriosCompativeis++;
            }
        }

        if (criteriosAplicaveis == 0) {
            return 0;
        }
        return Math.round(100f * criteriosCompativeis / criteriosAplicaveis);
    }

    private boolean orcamentosProximos(BigDecimal orcamentoA, BigDecimal orcamentoB) {
        BigDecimal maior = orcamentoA.max(orcamentoB);
        if (maior.compareTo(BigDecimal.ZERO) == 0) {
            return true; // ambos zero: sem diferença relativa possível, considera compatível
        }
        BigDecimal diferenca = orcamentoA.subtract(orcamentoB).abs();
        BigDecimal percentualDiferenca = diferenca.divide(maior, 4, RoundingMode.HALF_UP);
        return percentualDiferenca.compareTo(TOLERANCIA_ORCAMENTO_PERCENTUAL) <= 0;
    }

    // Mesmo padrão usado em UserServiceImpl (duplicado aqui de propósito por
    // simplicidade — ver comentário lá; candidato a virar um bean
    // compartilhado quando o T5.3 trouxer autenticação de verdade).
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
