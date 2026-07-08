package com.campusliving.service.interacao;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.campusliving.dto.interacao.ContatoResponseDTO;
import com.campusliving.dto.interacao.InteresseRequestDTO;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.interacao.Contato;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.usuario.UserRepository;

@Service
public class ContatoServiceImpl implements ContatoService {

    private final ContatoRepository contatoRepository;
    private final UserRepository userRepository;

    public ContatoServiceImpl(ContatoRepository contatoRepository, UserRepository userRepository) {
        this.contatoRepository = contatoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ContatoResponseDTO registrarInteresse(InteresseRequestDTO dto, UUID estudanteId) {
        // Placeholder de autenticação (mesmo esquema do UserServiceImpl):
        // sem T5.3, "quem está logado" vem do header X-User-Id.
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
        return new ContatoResponseDTO(contato);
    }
}
