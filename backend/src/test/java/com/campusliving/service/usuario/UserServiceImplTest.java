package com.campusliving.service.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import com.campusliving.dto.interacao.FavoritoResponseDTO;
import com.campusliving.dto.usuario.UserPublicProfileDTO;
import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.dto.usuario.UserUpdateRequestDTO;
import com.campusliving.dto.usuario.VerificacaoLocadorResponseDTO;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.interacao.FavoritoDuplicadoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.TipoContaInvalidoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.exception.usuario.VerificacaoJaPendenteException;
import com.campusliving.model.interacao.Favorito;
import com.campusliving.model.usuario.User;
import com.campusliving.model.usuario.VerificacaoLocador;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.interacao.FavoritoRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.repository.usuario.VerificacaoLocadorRepository;

/**
 * Testes unitários dos métodos novos do T5.4 (o restante de UserServiceImpl —
 * criar/getUserById/listUsers — já existia antes do T5.4 e não é escopo
 * desta suíte).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificacaoLocadorRepository verificacaoLocadorRepository;
    @Mock
    private FavoritoRepository favoritoRepository;
    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private DocumentStorageService documentStorageService;

    private UserServiceImpl service;

    private UUID donoId;
    private UUID adminId;
    private UUID estranhoId;
    private User dono;
    private User admin;
    private User estranho;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                userRepository,
                new ModelMapper(),
                verificacaoLocadorRepository,
                favoritoRepository,
                contatoRepository,
                documentStorageService
        );

        donoId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        estranhoId = UUID.randomUUID();

        dono = User.builder()
                .id(donoId)
                .nome("Estudante Dono")
                .email("dono@ufcg.edu.br")
                .tipoConta(User.Tipo.ESTUDANTE)
                .bio("bio antiga")
                .telefone("83999990000")
                .verificado(true)
                .ativo(true)
                .build();

        admin = User.builder()
                .id(adminId)
                .nome("Admin")
                .email("admin@ufcg.edu.br")
                .tipoConta(User.Tipo.ADMIN)
                .ativo(true)
                .build();

        estranho = User.builder()
                .id(estranhoId)
                .nome("Estranho")
                .email("estranho@ufcg.edu.br")
                .tipoConta(User.Tipo.ESTUDANTE)
                .ativo(true)
                .build();
    }

    // --- atualizarPerfil (RF-06) -----------------------------------------

    @Test
    void atualizarPerfil_quandoDonoAtualiza_deveAtualizarApenasCamposEnviados() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        UserUpdateRequestDTO dto = UserUpdateRequestDTO.builder()
                .nome("Novo Nome")
                .curso("Ciencia da Computacao")
                .build();

        UserResponseDTO resultado = service.atualizarPerfil(donoId, dto, donoId);

        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
        assertThat(resultado.getCurso()).isEqualTo("Ciencia da Computacao");
        // bio não veio no DTO -> não deve ser sobrescrita
        assertThat(dono.getBio()).isEqualTo("bio antiga");
        verify(userRepository).save(dono);
    }

    @Test
    void atualizarPerfil_quandoAdminAtualiza_devePermitir() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        UserUpdateRequestDTO dto = UserUpdateRequestDTO.builder().bio("editado pelo admin").build();

        UserResponseDTO resultado = service.atualizarPerfil(donoId, dto, adminId);

        assertThat(resultado.getBio()).isEqualTo("editado pelo admin");
    }

    @Test
    void atualizarPerfil_quandoNaoEhDonoNemAdmin_deveLancarAcessoNegado() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));

        UserUpdateRequestDTO dto = UserUpdateRequestDTO.builder().nome("Hackeado").build();

        assertThatThrownBy(() -> service.atualizarPerfil(donoId, dto, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void atualizarPerfil_quandoUsuarioNaoExiste_deveLancarUserNotFound() {
        when(userRepository.findById(donoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarPerfil(donoId, UserUpdateRequestDTO.builder().build(), donoId))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- getPerfilPublico (RF-07 / RNF-LEG-03) ---------------------------

    @Test
    void getPerfilPublico_quandoEhOProprio_deveLiberarContato() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        UserPublicProfileDTO perfil = service.getPerfilPublico(donoId, donoId);

        assertThat(perfil.isContatoLiberado()).isTrue();
        assertThat(perfil.getEmail()).isEqualTo(dono.getEmail());
    }

    @Test
    void getPerfilPublico_quandoEstranhoSemContatoPrevio_deveMascarar() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));
        when(contatoRepository.existeContatoEntre(estranhoId, donoId)).thenReturn(false);

        UserPublicProfileDTO perfil = service.getPerfilPublico(donoId, estranhoId);

        assertThat(perfil.isContatoLiberado()).isFalse();
        assertThat(perfil.getEmail()).isNull();
        assertThat(perfil.getTelefone()).isNull();
    }

    @Test
    void getPerfilPublico_quandoAdminConsulta_deveLiberarContato() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        UserPublicProfileDTO perfil = service.getPerfilPublico(donoId, adminId);

        assertThat(perfil.isContatoLiberado()).isTrue();
    }

    @Test
    void getPerfilPublico_quandoContatoPrevioExiste_deveLiberarContato() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));
        when(contatoRepository.existeContatoEntre(estranhoId, donoId)).thenReturn(true);

        UserPublicProfileDTO perfil = service.getPerfilPublico(donoId, estranhoId);

        assertThat(perfil.isContatoLiberado()).isTrue();
        assertThat(perfil.getEmail()).isEqualTo(dono.getEmail());
    }

    @Test
    void getPerfilPublico_quandoRequesterNaoInformado_deveMascarar() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        UserPublicProfileDTO perfil = service.getPerfilPublico(donoId, null);

        assertThat(perfil.isContatoLiberado()).isFalse();
    }

    // --- solicitarVerificacao (RF-08/09) ----------------------------------

    @Test
    void solicitarVerificacao_quandoSemPendente_deveCriarComStatusPendente() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(verificacaoLocadorRepository.findByUserIdAndStatus(donoId, VerificacaoLocador.Status.PENDENTE.name()))
                .thenReturn(List.of());
        when(documentStorageService.salvar(any(), any())).thenReturn("/uploads/verificacoes/doc.pdf");

        VerificacaoLocadorResponseDTO resultado = service.solicitarVerificacao(donoId, null, donoId);

        assertThat(resultado.getStatus()).isEqualTo(VerificacaoLocador.Status.PENDENTE.name());
        assertThat(resultado.getDocumentoUrl()).isEqualTo("/uploads/verificacoes/doc.pdf");
        verify(verificacaoLocadorRepository).save(any(VerificacaoLocador.class));
    }

    @Test
    void solicitarVerificacao_quandoJaTemPendente_deveLancarVerificacaoJaPendente() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(verificacaoLocadorRepository.findByUserIdAndStatus(donoId, VerificacaoLocador.Status.PENDENTE.name()))
                .thenReturn(List.of(VerificacaoLocador.builder().build()));

        assertThatThrownBy(() -> service.solicitarVerificacao(donoId, null, donoId))
                .isInstanceOf(VerificacaoJaPendenteException.class);
        verify(verificacaoLocadorRepository, never()).save(any());
    }

    // --- promoverContaMista (RF-18) ---------------------------------------

    @Test
    void promoverContaMista_quandoEstudante_devePromoverParaMisto() {
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        service.promoverContaMista(donoId, donoId);

        assertThat(dono.getTipoConta()).isEqualTo(User.Tipo.MISTO);
        verify(userRepository).save(dono);
    }

    @Test
    void promoverContaMista_quandoNaoEhEstudante_deveLancarTipoContaInvalido() {
        dono.setTipoConta(User.Tipo.LOCADOR);
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        assertThatThrownBy(() -> service.promoverContaMista(donoId, donoId))
                .isInstanceOf(TipoContaInvalidoException.class);
    }

    // --- favoritos (RF-26/27) --------------------------------------------

    @Test
    void adicionarFavorito_quandoAnuncioExiste_deveSalvar() {
        UUID adId = UUID.randomUUID();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(favoritoRepository.anuncioExiste(adId)).thenReturn(true);

        FavoritoResponseDTO resultado = service.adicionarFavorito(donoId, adId, donoId);

        assertThat(resultado.getAdId()).isEqualTo(adId);
        verify(favoritoRepository).save(any(Favorito.class));
    }

    @Test
    void adicionarFavorito_quandoAnuncioNaoExiste_deveLancarAnuncioNaoEncontrado() {
        UUID adId = UUID.randomUUID();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(favoritoRepository.anuncioExiste(adId)).thenReturn(false);

        assertThatThrownBy(() -> service.adicionarFavorito(donoId, adId, donoId))
                .isInstanceOf(AnuncioNaoEncontradoException.class);
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void adicionarFavorito_quandoDuplicado_deveLancarFavoritoDuplicado() {
        UUID adId = UUID.randomUUID();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(favoritoRepository.anuncioExiste(adId)).thenReturn(true);
        when(favoritoRepository.save(any())).thenThrow(new DataIntegrityViolationException("uq_favorites_user_ad"));

        assertThatThrownBy(() -> service.adicionarFavorito(donoId, adId, donoId))
                .isInstanceOf(FavoritoDuplicadoException.class);
    }

    @Test
    void adicionarFavorito_quandoNaoEhDono_deveLancarAcessoNegado() {
        UUID adId = UUID.randomUUID();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        assertThatThrownBy(() -> service.adicionarFavorito(donoId, adId, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(favoritoRepository, never()).anuncioExiste(any());
    }

    @Test
    void listarFavoritos_deveMapearListaDoRepository() {
        UUID adId = UUID.randomUUID();
        Favorito favorito = Favorito.builder().id(UUID.randomUUID()).userId(donoId).adId(adId).build();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(favoritoRepository.findByUserId(donoId)).thenReturn(List.of(favorito));

        List<FavoritoResponseDTO> resultado = service.listarFavoritos(donoId, donoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getAdId()).isEqualTo(adId);
    }

    @Test
    void removerFavorito_quandoDono_deveDelegarAoRepository() {
        UUID adId = UUID.randomUUID();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        service.removerFavorito(donoId, adId, donoId);

        verify(favoritoRepository, times(1)).deleteByUserIdAndAdId(donoId, adId);
    }

    @Test
    void removerFavorito_quandoNaoEhDono_deveLancarAcessoNegado() {
        UUID adId = UUID.randomUUID();
        when(userRepository.findById(donoId)).thenReturn(Optional.of(dono));

        assertThatThrownBy(() -> service.removerFavorito(donoId, adId, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(favoritoRepository, never()).deleteByUserIdAndAdId(any(), any());
    }

    // --- excluirUsuario (RNF/LEG-02) ---------------------------------------

    @Test
    void excluirUsuario_quandoDono_deveDeletar() {
        // Não precisa stubar findById aqui: como requesterId == id, o check
        // de autorização é resolvido só por comparação de UUID (ver
        // UserServiceImpl#exigirDonoOuAdmin), sem consultar o repository de
        // novo.
        when(userRepository.existsById(donoId)).thenReturn(true);

        service.excluirUsuario(donoId, donoId);

        verify(userRepository).deleteById(donoId);
    }

    @Test
    void excluirUsuario_quandoUsuarioNaoExiste_deveLancarUserNotFound() {
        when(userRepository.existsById(donoId)).thenReturn(false);

        assertThatThrownBy(() -> service.excluirUsuario(donoId, donoId))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void excluirUsuario_quandoNaoAutorizado_deveLancarAcessoNegado() {
        when(userRepository.existsById(donoId)).thenReturn(true);
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));

        assertThatThrownBy(() -> service.excluirUsuario(donoId, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(userRepository, never()).deleteById(any());
    }
}
