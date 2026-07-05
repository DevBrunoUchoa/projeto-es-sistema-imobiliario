package com.campusliving.service.usuario;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.model.usuario.User;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository){
        this.repository = repository;
    }



	@Override
	public User getUserById(Long userId) {
        User usuario = repository.findById(userId).orElseThrow(UserNotFoundException::new);
        return usuario;
    }

	@Override
	public List<UserResponseDTO> listUsers() {
		List<User> usuarios = repository.findAll();
        return usuarios.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
	}
}
