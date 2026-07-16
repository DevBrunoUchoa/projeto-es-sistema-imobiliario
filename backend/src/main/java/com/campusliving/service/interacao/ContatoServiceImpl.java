package com.campusliving.service.interacao;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.campusliving.dto.interacao.ContatoResponseDTO;
import com.campusliving.dto.interacao.InteresseRequestDTO;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.interacao.Contato;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.email.EmailService;

@Service
public class ContatoServiceImpl implements ContatoService {

    private static final int PREVIEW_MAX_CHARS = 200; // RF-38: preview da mensagem

    private final ContatoRepository contatoRepository;
    private final UserRepository userRepository;
    private final AnuncioRepository anuncioRepository;
    private final EmailService emailService;

    public ContatoServiceImpl(ContatoRepository contatoRepository, UserRepository userRepository,
            AnuncioRepository anuncioRepository, EmailService emailService) {
        this.contatoRepository = contatoRepository;
        this.userRepository = userRepository;
        this.anuncioRepository = anuncioRepository;
        this.emailService = emailService;
    }

    @Override
    public ContatoResponseDTO registrarInteresse(InteresseRequestDTO dto, UUID estudanteId) {
        // O requerente vem do usuário autenticado (JWT); ver ContatoController.
        if (estudanteId == null) {
            throw new AcessoNegadoException();
        }
        if (!userRepository.existsById(estudanteId)) {
            throw new UserNotFoundException();
        }
        if (!contatoRepository.anuncioExiste(dto.getAdId())) {
            throw new AnuncioNaoEncontradoException();
        }

        Contato contato = Contato.builder()
                .estudanteId(estudanteId)
                .adId(dto.getAdId())
                .mensagem(dto.getMensagem())
                .status(Contato.Status.ENVIADO.name())
                .build();
        contatoRepository.save(contato);

        // A partir daqui, ContatoRepository#existeContatoEntre passa a
        // retornar true para este par (estudante, locador do anúncio) — é
        // isso que libera o contato do locador no GET /usuarios/:id/publico
        // (RNF/LEG-03).

        notificarLocador(dto, estudanteId);

        return new ContatoResponseDTO(contato);
    }

    /** RF-38: envia e-mail ao locador avisando do novo interesse (best-effort). */
    private void notificarLocador(InteresseRequestDTO dto, UUID estudanteId) {
        Anuncio anuncio = anuncioRepository.findById(dto.getAdId()).orElse(null);
        if (anuncio == null) {
            return;
        }
        User locador = userRepository.findById(anuncio.getLocadorId()).orElse(null);
        if (locador == null) {
            return;
        }
        String estudanteNome = userRepository.findById(estudanteId)
                .map(User::getNome)
                .orElse("Um estudante");

        emailService.enviarNotificacaoInteresse(
                locador.getEmail(),
                locador.getNome(),
                estudanteNome,
                anuncio.getTitulo(),
                previewMensagem(dto.getMensagem()));
    }

    private String previewMensagem(String mensagem) {
        if (mensagem == null) {
            return "";
        }
        return mensagem.length() <= PREVIEW_MAX_CHARS
                ? mensagem
                : mensagem.substring(0, PREVIEW_MAX_CHARS) + "...";
    }
}
