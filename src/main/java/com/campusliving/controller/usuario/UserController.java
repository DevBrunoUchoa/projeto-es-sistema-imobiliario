package com.campusliving.controller.usuario;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.usuario.UserResponseDTO;
import com.campusliving.service.usuario.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
            
    private final UserService userService;

    public UserController(UserService service){
        this.userService = service;
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUsers(@PathVariable Long userId){
        return userService.getUserById(userId);
    }
    
    @GetMapping
    public List<UserResponseDTO> listUsers(){
        return userService.listUsers();
    }
}
