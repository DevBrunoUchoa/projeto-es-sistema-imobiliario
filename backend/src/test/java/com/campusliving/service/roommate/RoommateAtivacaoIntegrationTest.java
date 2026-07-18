package com.campusliving.service.roommate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.TestcontainersConfiguration;
import com.campusliving.dto.roommate.PerfilRoommateRequestDTO;
import com.campusliving.dto.roommate.PreferenciasRoommateRequestDTO;
import com.campusliving.exception.roommate.PerfilRoommateIncompletoException;
import com.campusliving.model.roommate.PerfilRoommate;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;

/**
 * RF-32: valida o fluxo de dois passos do front (salvar preferências e depois
 * tornar o perfil público), garantindo que o barulho/horário persistidos pelo
 * primeiro passo são vistos pela validação do segundo.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class RoommateAtivacaoIntegrationTest {

    @Autowired
    private RoommateService roommateService;
    @Autowired
    private UserRepository userRepository;

    private UUID novoUsuario() {
        User u = userRepository.saveAndFlush(User.builder()
                .nome("Roomie")
                .email("roomie-" + UUID.randomUUID() + "@teste.com")
                .senhaHash("hash-fake")
                .tipoConta(User.Tipo.ESTUDANTE)
                .build());
        return u.getId();
    }

    @Test
    void salvarPreferenciasComBarulhoEHorario_permiteTornarPublico() {
        UUID id = novoUsuario();

        roommateService.salvarPreferencias(id, PreferenciasRoommateRequestDTO.builder()
                .nivelBarulho(PerfilRoommate.NivelBarulho.SILENCIOSO)
                .horarioDorme(LocalTime.of(23, 0))
                .horarioAcorda(LocalTime.of(7, 0))
                .fumante(false)
                .aceitaPets(true)
                .build(), id);

        assertThatCode(() -> roommateService.ativarPerfil(PerfilRoommateRequestDTO.builder()
                .descricao("Busco roommate tranquilo")
                .perfilVisivel(true)
                .jaPossuiCasa(false)
                .build(), id))
                .doesNotThrowAnyException();
    }

    @Test
    void tornarPublicoSemPreencherBarulhoEHorario_bloqueia() {
        UUID id = novoUsuario();

        assertThatThrownBy(() -> roommateService.ativarPerfil(PerfilRoommateRequestDTO.builder()
                .descricao("Sem preferencias ainda")
                .perfilVisivel(true)
                .jaPossuiCasa(false)
                .build(), id))
                .isInstanceOf(PerfilRoommateIncompletoException.class);
    }

    @Test
    void perfilFicaVisivelAposAtivacaoCompleta() {
        UUID id = novoUsuario();
        roommateService.salvarPreferencias(id, PreferenciasRoommateRequestDTO.builder()
                .nivelBarulho(PerfilRoommate.NivelBarulho.MODERADO)
                .horarioDorme(LocalTime.of(0, 30))
                .build(), id);

        var resp = roommateService.ativarPerfil(PerfilRoommateRequestDTO.builder()
                .perfilVisivel(true)
                .build(), id);

        assertThat(resp.isPerfilVisivel()).isTrue();
    }
}
