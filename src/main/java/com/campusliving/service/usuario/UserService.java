package com.campusliving.service.usuario;

import java.util.List;

import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.model.usuario.User;


public interface UserService{

    public User getUserById(Long userId);

    public List<UserResponseDTO> listUsers();

}
