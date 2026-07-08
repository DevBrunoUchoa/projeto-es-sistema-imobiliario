package com.campusliving.repository.roommate;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.roommate.RoommateMatch;

public interface RoommateMatchRepository extends JpaRepository<RoommateMatch, UUID> {
}
