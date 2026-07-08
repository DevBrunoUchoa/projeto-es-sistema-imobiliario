package com.campusliving.repository.usuario;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.usuario.User;

public interface UserRepository extends JpaRepository<User, UUID>{
    List<User> findByEmail(String email);
}
