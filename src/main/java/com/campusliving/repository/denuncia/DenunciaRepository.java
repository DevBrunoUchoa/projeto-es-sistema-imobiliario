package com.campusliving.repository.denuncia;

import com.campusliving.model.denuncia.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {

    List<Denuncia> findByDenuncianteIdAndAlvoIdAndStatus(UUID denuncianteId, UUID alvoId, Denuncia.Status status);

    List<Denuncia> findByStatus(Denuncia.Status status);

    long countByAlvoIdAndStatus(UUID alvoId, Denuncia.Status status);

    long countByStatus(Denuncia.Status status);
}