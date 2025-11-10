package com.devbuild.inscriptionservice.repository;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import com.devbuild.inscriptionservice.model.Campagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampagneRepository extends JpaRepository<Campagne, String> {

    /**
     * Trouve une campagne active pour un type donné à la date actuelle.
     * C'est la logique métier principale pour vérifier si les inscriptions sont ouvertes.
     */
    @Query("SELECT c FROM Campagne c WHERE c.type = :type AND c.active = true AND :now BETWEEN c.dateDebut AND c.dateFin")
    Optional<Campagne> findActiveCampaign(@Param("type") InscriptionType type, @Param("now") LocalDateTime now);

    List<Campagne> findByAnneeAcademique(AnneeAcademique annee);
}