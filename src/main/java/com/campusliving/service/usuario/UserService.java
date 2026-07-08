package com.campusliving.service.usuario;

import java.util.List;
import java.util.UUID;

import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.model.usuario.User;
import com.campusliving.dto.usuario.UserPostPutRequestDTO;

public interface UserService{

    public UserResponseDTO criar(UserPostPutRequestDTO usuarioPostPutRequestDTO);

    public User getUserById(UUID userId);

    public List<UserResponseDTO> listUsers();

}
