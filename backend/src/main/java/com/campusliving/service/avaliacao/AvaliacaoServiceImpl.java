package com.campusliving.service.avaliacao;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.dto.avaliacao.AvaliacaoRequestDTO;
import com.campusliving.dto.avaliacao.AvaliacaoResponseDTO;
import com.campusliving.dto.avaliacao.RespostaLocadorRequestDTO;
import com.campusliving.exception.avaliacao.AutoAvaliacaoException;
import com.campusliving.exception.avaliacao.AvaliacaoDuplicadaException;
import com.campusliving.exception.avaliacao.AvaliacaoNaoEncontradaException;
import com.campusliving.exception.avaliacao.ComentarioImproprioException;
import com.campusliving.exception.avaliacao.VinculoNaoEncontradoException;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.avaliacao.Avaliacao;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.avaliacao.AvaliacaoRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.usuario.UserRepository;

@Service
public class AvaliacaoServiceImpl implements AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AnuncioRepository anuncioRepository;
    private final UserRepository userRepository;
    private final ContatoRepository contatoRepository;
    private final PalavraoFilter palavraoFilter;

    public AvaliacaoServiceImpl(
            AvaliacaoRepository avaliacaoRepository,
            AnuncioRepository anuncioRepository,
            UserRepository userRepository,
            ContatoRepository contatoRepository,
            PalavraoFilter palavraoFilter) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.anuncioRepository = anuncioRepository;
        this.userRepository = userRepository;
        this.contatoRepository = contatoRepository;
        this.palavraoFilter = palavraoFilter;
    }

    @Override
    @Transactional
    public AvaliacaoResponseDTO publicar(AvaliacaoRequestDTO dto, UUID avaliadorId) {
        // Placeholder de autenticação (mesmo esquema do UserController/
        // ContatoServiceImpl): sem T5.3, "quem está logado" vem do X-User-Id.
        if (avaliadorId == null) {
            throw new AcessoNegadoException();
        }
        if (!userRepository.existsById(avaliadorId)) {
            throw new UserNotFoundException();
        }

        Anuncio anuncio = anuncioRepository.findById(dto.getAdId())
                .orElseThrow(AnuncioNaoEncontradoException::new);
        UUID locadorId = anuncio.getLocadorId();

        // Espelha chk_reviews_nao_auto_avaliacao — checado aqui antes para
        // devolver uma mensagem amigável em vez de deixar o INSERT falhar na
        // CHECK constraint do banco.
        if (avaliadorId.equals(locadorId)) {
            throw new AutoAvaliacaoException();
        }

        // RF-29: "avaliador deve ter tido vínculo com o anunciante". O
        // vínculo é o mesmo que a RNF/LEG-03 usa para liberar contato do
        // locador — reaproveitamos ContatoRepository em vez de confiar em um
        // campo "contatoPrevio" que o próprio cliente declarasse no corpo.
        if (!contatoRepository.existeContatoEntre(avaliadorId, locadorId)) {
            throw new VinculoNaoEncontradoException();
        }

        // Checagem prévia — a garantia definitiva contra corrida entre
        // requisições concorrentes é o índice único parcial do banco (ver
        // catch de DataIntegrityViolationException abaixo).
        if (avaliacaoRepository.existsByAvaliadorIdAndAdId(avaliadorId, dto.getAdId())) {
            throw new AvaliacaoDuplicadaException();
        }

        if (palavraoFilter.contemPalavraImpropria(dto.getComentario())) {
            throw new ComentarioImproprioException();
        }

        Avaliacao avaliacao = Avaliacao.builder()
                .avaliadorId(avaliadorId)
                .avaliadoId(locadorId)
                .adId(dto.getAdId())
                .nota(dto.getNota().shortValue())
                .comentario(dto.getComentario())
                .contatoPrevio(true)
                .build();

        try {
            avaliacao = avaliacaoRepository.save(avaliacao);
        } catch (DataIntegrityViolationException e) {
            // Duas requisições passaram pela checagem de existsBy acima ao
            // mesmo tempo; quem chega no INSERT depois esbarra no índice
            // único parcial uq_reviews_avaliador_ad e cai aqui.
            throw new AvaliacaoDuplicadaException();
        }

        // A partir daqui, o trigger trg_reviews_recalcular_reputacao
        // (V19__add_reputacao_users.sql) já atualizou users.nota_media e
        // users.total_avaliacoes do locador dentro desta mesma transação.
        return new AvaliacaoResponseDTO(avaliacao);
    }

    @Override
    @Transactional
    public AvaliacaoResponseDTO responder(UUID avaliacaoId, RespostaLocadorRequestDTO dto, UUID locadorId) {
        if (locadorId == null) {
            throw new AcessoNegadoException();
        }

        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(AvaliacaoNaoEncontradaException::new);

        // avaliadoId pode ser null (RNF/LEG-02: locador excluiu a conta) —
        // nesse caso não há mais ninguém autorizado a responder.
        if (avaliacao.getAvaliadoId() == null || !avaliacao.getAvaliadoId().equals(locadorId)) {
            throw new AcessoNegadoException();
        }

        // RF-31: "permite apenas 1 resposta por review (edição posterior
        // permitida)" — publicar e editar são a mesma operação de
        // substituição, por isso não há checagem de "já existe resposta"
        // aqui; o PUT sempre sobrescreve o valor atual.
        avaliacao.setRespostaLocador(dto.getResposta());
        Avaliacao atualizada = avaliacaoRepository.save(avaliacao);

        return new AvaliacaoResponseDTO(atualizada);
    }

    @Override
    public Page<AvaliacaoResponseDTO> listarPorAnuncio(UUID adId, Pageable pageable) {
        if (!anuncioRepository.existsById(adId)) {
            throw new AnuncioNaoEncontradoException();
        }
        return avaliacaoRepository.findByAdId(adId, pageable)
                .map(AvaliacaoResponseDTO::new);
    }

    @Override
    public Page<AvaliacaoResponseDTO> listarPorLocador(UUID avaliadoId, Pageable pageable) {
        if (!userRepository.existsById(avaliadoId)) {
            throw new UserNotFoundException();
        }
        return avaliacaoRepository.findByAvaliadoId(avaliadoId, pageable)
                .map(AvaliacaoResponseDTO::new);
    }

    @Override
    public Page<AvaliacaoResponseDTO> listarMinhasAvaliacoes(UUID avaliadorId, Pageable pageable) {
        if (avaliadorId == null) {
            throw new AcessoNegadoException();
        }
        return avaliacaoRepository.findByAvaliadorId(avaliadorId, pageable)
                .map(AvaliacaoResponseDTO::new);
    }
}