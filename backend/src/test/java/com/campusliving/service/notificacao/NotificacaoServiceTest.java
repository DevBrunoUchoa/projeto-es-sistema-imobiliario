package com.campusliving.service.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.campusliving.dto.notificacao.NotificacaoResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.notificacao.Notificacao;
import com.campusliving.repository.notificacao.NotificacaoRepository;

/** Testes unitários da leitura de notificações in-app (RF-39). */
@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository repository;

    private NotificacaoService service;

    @BeforeEach
    void setUp() {
        service = new NotificacaoService(repository);
    }

    @Test
    void listar_quandoAutenticado_deveMapearPaginaOrdenadaDoRepositorio() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Notificacao notificacao = notificacao(UUID.randomUUID(), userId, false);
        when(repository.findByUserIdOrderByDataCriacaoDesc(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(notificacao), pageable, 1));

        Page<NotificacaoResponseDTO> resultado = service.listar(userId, pageable);

        assertThat(resultado.getContent()).singleElement().satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(notificacao.getId());
            assertThat(dto.tipo()).isEqualTo(Notificacao.Tipo.MATCH.name());
            assertThat(dto.lida()).isFalse();
        });
    }

    @Test
    void contarNaoLidas_quandoAutenticado_deveDelegarAoRepositorio() {
        UUID userId = UUID.randomUUID();
        when(repository.countByUserIdAndLidaFalse(userId)).thenReturn(3L);

        long total = service.contarNaoLidas(userId);

        assertThat(total).isEqualTo(3L);
        verify(repository).countByUserIdAndLidaFalse(userId);
    }

    @Test
    void marcarComoLida_quandoNotificacaoDoUsuarioExiste_devePersistirLeitura() {
        UUID userId = UUID.randomUUID();
        UUID notificacaoId = UUID.randomUUID();
        Notificacao notificacao = notificacao(notificacaoId, userId, false);
        when(repository.findByIdAndUserId(notificacaoId, userId)).thenReturn(Optional.of(notificacao));

        NotificacaoResponseDTO resultado = service.marcarComoLida(notificacaoId, userId);

        assertThat(notificacao.isLida()).isTrue();
        assertThat(resultado.lida()).isTrue();
        verify(repository).save(notificacao);
    }

    @Test
    void marcarComoLida_quandoNotificacaoNaoPertenceAoUsuario_deveRetornarNaoEncontrada() {
        UUID userId = UUID.randomUUID();
        UUID notificacaoId = UUID.randomUUID();
        when(repository.findByIdAndUserId(notificacaoId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoLida(notificacaoId, userId))
                .isInstanceOf(ProjectException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void marcarTodasComoLidas_quandoAutenticado_deveRetornarQuantidadeAtualizada() {
        UUID userId = UUID.randomUUID();
        when(repository.marcarTodasComoLidas(userId)).thenReturn(2);

        int total = service.marcarTodasComoLidas(userId);

        assertThat(total).isEqualTo(2);
        verify(repository).marcarTodasComoLidas(userId);
    }

    @Test
    void listar_semUsuarioAutenticado_deveNegarAcessoAntesDeConsultarRepositorio() {
        assertThatThrownBy(() -> service.listar(null, PageRequest.of(0, 10)))
                .isInstanceOf(AcessoNegadoException.class);

        verify(repository, never()).findByUserIdOrderByDataCriacaoDesc(any(), any());
    }

    private Notificacao notificacao(UUID id, UUID userId, boolean lida) {
        return Notificacao.builder()
                .id(id)
                .userId(userId)
                .tipo(Notificacao.Tipo.MATCH)
                .titulo("Novo match")
                .mensagem("Você tem um novo match")
                .lida(lida)
                .dataCriacao(OffsetDateTime.now())
                .build();
    }
}
