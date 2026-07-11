package com.campusliving.repository.roommate;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.roommate.RoommateMatch;

public interface RoommateMatchRepository extends JpaRepository<RoommateMatch, UUID> {

    // RF-34: a UNIQUE do banco (uq_roommate_match_par) só cobre o par exato
    // (A pede a B); isso aqui cobre os dois sentidos (A->B e B->A), que é a
    // checagem que realmente importa pra "bloquear nova solicitação se já
    // existe entre o par" — ver nota em V8__create_roommate_matches.sql.
    @Query("SELECT m FROM RoommateMatch m WHERE "
            + "(m.solicitanteId = :a AND m.destinatarioId = :b) "
            + "OR (m.solicitanteId = :b AND m.destinatarioId = :a)")
    List<RoommateMatch> findByParEmQualquerSentido(@Param("a") UUID a, @Param("b") UUID b);
}
