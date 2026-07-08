package com.campusliving.repository.denuncia;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.denuncia.Denuncia;

public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {
}
