package org.example.signer.repository;

import org.example.signer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrUsername(String email, String username);

    Optional<User> findByMicrosoftOid(String microsoftOid);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
