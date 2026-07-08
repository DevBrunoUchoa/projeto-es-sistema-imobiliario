package com.campusliving.service.interacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusliving.dto.interacao.ContatoResponseDTO;
import com.campusliving.dto.interacao.InteresseRequestDTO;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.interacao.Contato;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.usuario.UserRepository;

/** Testes unitários do T5.4.5 (POST /interesses — RF-28, RNF/LEG-03). */
@ExtendWith(MockitoExtension.class)
class ContatoServiceImplTest {

    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private UserRepository userRepository;

    private ContatoServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new ContatoServiceImpl(contatoRepository, userRepository);
    }

    @Test
    void registrarInteresse_quandoTudoValido_deveCriarContatoComStatusEnviado() {
        UUID estudanteId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        InteresseRequestDTO dto = InteresseRequestDTO.builder()
                .adId(adId)
                .mensagem("Tenho interesse neste anuncio")
                .build();

        when(userRepository.existsById(estudanteId)).thenReturn(true);
        when(contatoRepository.anuncioExiste(adId)).thenReturn(true);

        ContatoResponseDTO resultado = service.registrarInteresse(dto, estudanteId);

        assertThat(resultado.getEstudanteId()).isEqualTo(estudanteId);
        assertThat(resultado.getAdId()).isEqualTo(adId);
        assertThat(resultado.getStatus()).isEqualTo(Contato.Status.ENVIADO.name());
        verify(contatoRepository).save(any(Contato.class));
    }

    @Test
    void registrarInteresse_semEstudanteAutenticado_deveLancarAcessoNegado() {
        InteresseRequestDTO dto = InteresseRequestDTO.builder()
                .adId(UUID.randomUUID())
                .mensagem("Interesse")
                .build();

        assertThatThrownBy(() -> service.registrarInteresse(dto, null))
                .isInstanceOf(AcessoNegadoException.class);
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void registrarInteresse_quandoEstudanteNaoExiste_deveLancarUserNotFound() {
        UUID estudanteId = UUID.randomUUID();
        InteresseRequestDTO dto = InteresseRequestDTO.builder()
                .adId(UUID.randomUUID())
                .mensagem("Interesse")
                .build();

        when(userRepository.existsById(estudanteId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrarInteresse(dto, estudanteId))
                .isInstanceOf(UserNotFoundException.class);
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void registrarInteresse_quandoAnuncioNaoExiste_deveLancarAnuncioNaoEncontrado() {
        UUID estudanteId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        InteresseRequestDTO dto = InteresseRequestDTO.builder()
                .adId(adId)
                .mensagem("Interesse")
                .build();

        when(userRepository.existsById(estudanteId)).thenReturn(true);
        when(contatoRepository.anuncioExiste(adId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrarInteresse(dto, estudanteId))
                .isInstanceOf(AnuncioNaoEncontradoException.class);
        verify(contatoRepository, never()).save(any());
    }
}
