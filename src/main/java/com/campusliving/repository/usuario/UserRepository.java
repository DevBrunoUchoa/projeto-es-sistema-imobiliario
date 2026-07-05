package com.campusliving.repository.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.usuario.User;

public interface UserRepository extends JpaRepository<User, Long>{
}
