package com.devbuild.soutenanceservice.repository;

import com.devbuild.soutenanceservice.model.SoutenanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoutenanceDocumentRepository extends JpaRepository<SoutenanceDocument, String> {
}