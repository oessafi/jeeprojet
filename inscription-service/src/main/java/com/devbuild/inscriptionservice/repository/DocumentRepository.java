package com.devbuild.inscriptionservice.repository;

import com.devbuild.inscriptionservice.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    // Trouver tous les documents liés à une inscription (sans les données binaires)
    // Spring Data fera une projection si nous utilisons DocumentInfoDTO
    List<Document> findByInscriptionId(String inscriptionId);
}