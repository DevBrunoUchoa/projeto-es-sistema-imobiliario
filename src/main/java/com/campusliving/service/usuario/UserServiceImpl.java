package com.campusliving.service.usuario;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.campusliving.dto.usuario.UserPostPutRequestDTO;
import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.exception.usuario.EmailEmUsoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.model.usuario.User;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository repository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository repository, ModelMapper model){
        this.repository = repository;
        this.modelMapper = model;
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



	@Override
	public UserResponseDTO criar(UserPostPutRequestDTO usuarioPostPutRequestDTO) {
		System.out.println(">>> DTO: " + usuarioPostPutRequestDTO);
        User usuario = modelMapper.map(usuarioPostPutRequestDTO, User.class);
        System.out.println(">>> User: " + usuario);
        List<User> users = repository.findByEmail(usuario.getEmail());
        if (users.size() != 0) {
            throw new EmailEmUsoException();
        }
        repository.save(usuario);
        return modelMapper.map(usuario, UserResponseDTO.class);
	}
}
