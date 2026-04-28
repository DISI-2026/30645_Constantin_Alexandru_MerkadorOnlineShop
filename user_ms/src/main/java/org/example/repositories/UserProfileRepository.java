package org.example.repositories;

import org.example.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>{
    List<UserProfile> findByFirstNameAndLastName(String firstName, String lastName);
}
