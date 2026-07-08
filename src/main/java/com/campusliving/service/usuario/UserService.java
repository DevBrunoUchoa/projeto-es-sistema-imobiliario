package com.campusliving.service.usuario;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.campusliving.dto.usuario.UserPostPutRequestDTO;
import com.campusliving.dto.usuario.UserPublicProfileDTO;
import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.dto.usuario.UserUpdateRequestDTO;
import com.campusliving.dto.usuario.VerificacaoLocadorResponseDTO;
import com.campusliving.dto.interacao.FavoritoResponseDTO;
import com.campusliving.model.usuario.User;

public interface UserService{

    public UserResponseDTO criar(UserPostPutRequestDTO usuarioPostPutRequestDTO);

    public User getUserById(UUID userId);

    public List<UserResponseDTO> listUsers();

    // --- T5.4: gerenciamento de usuários --------------------------------

    /** RF-06: apenas o próprio usuário ou ADMIN. */
    public UserResponseDTO atualizarPerfil(UUID id, UserUpdateRequestDTO dto, UUID requesterId);

    /** RF-07/RNF-LEG-03: mascara email/telefone salvo dono, ADMIN ou contato prévio liberado. */
    public UserPublicProfileDTO getPerfilPublico(UUID id, UUID requesterId);

    /** RF-08/RF-09: upload de documento + solicitação de verificação de locador. */
    public VerificacaoLocadorResponseDTO solicitarVerificacao(UUID id, MultipartFile documento, UUID requesterId);

    /** RF-18: promove ESTUDANTE -> MISTO. */
    public void promoverContaMista(UUID id, UUID requesterId);

    /** RF-26/RF-27: favoritos. */
    public FavoritoResponseDTO adicionarFavorito(UUID id, UUID adId, UUID requesterId);
    public List<FavoritoResponseDTO> listarFavoritos(UUID id, UUID requesterId);
    public void removerFavorito(UUID id, UUID adId, UUID requesterId);

    /** RNF/LEG-02: exclusão permanente (cascades/anonimização definidos no schema, ver migrations). */
    public void excluirUsuario(UUID id, UUID requesterId);

}
