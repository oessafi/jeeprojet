package com.devbuild.inscriptionservice.repository;

import com.devbuild.inscriptionservice.enums.InscriptionStatus;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import com.devbuild.inscriptionservice.model.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, String> {

    // Spring Data JPA créera automatiquement les requêtes pour ces méthodes
    List<Inscription> findByDoctorantId(String doctorantId);

    List<Inscription> findByStatus(InscriptionStatus status);

    java.util.Optional<Inscription> findFirstByDoctorantIdAndTypeOrderByDateCreationAsc(String doctorantId, InscriptionType type);
}