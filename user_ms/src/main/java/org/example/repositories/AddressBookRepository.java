package org.example.repositories;

import org.example.entities.AddressBook;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressBookRepository extends JpaRepository<AddressBook, UUID> {
    List<AddressBook> findAllByUserProfileUserId(UUID userId);
    Optional<AddressBook> findByUserProfileUserIdAndIsDefaultTrue(UUID userId);
}