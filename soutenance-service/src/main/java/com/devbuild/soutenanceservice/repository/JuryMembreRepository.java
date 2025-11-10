package com.devbuild.soutenanceservice.repository;

import com.devbuild.soutenanceservice.model.JuryMembre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JuryMembreRepository extends JpaRepository<JuryMembre, String> {
    // Vous pouvez ajouter des recherches personnalisées ici si nécessaire
}