package com.campusliving.repository.usuario;

import com.campusliving.model.usuario.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findByEmail(String email);

    long countByTipoContaAndVerificado(User.Tipo tipo, boolean verificado);
}