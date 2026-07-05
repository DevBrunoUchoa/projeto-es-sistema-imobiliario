package com.campusliving.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> getUsers(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.listUsers());
    }
}
