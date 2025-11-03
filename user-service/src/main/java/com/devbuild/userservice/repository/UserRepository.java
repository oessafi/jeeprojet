package com.devbuild.userservice.repository;

import com.devbuild.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Spring Data JPA va automatiquement créer la requête
    Optional<User> findByEmail(String email);
}