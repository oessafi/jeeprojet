package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.dto.*;
import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionStatus;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InscriptionServiceImpl implements InscriptionService {

    private final Map<String, InscriptionDTO> inscriptionStore = new ConcurrentHashMap<>();

    public InscriptionServiceImpl() {
        initializeTestData();
    }

    private void initializeTestData() {
        log.info("Initialisation des données de test pour les inscriptions...");

        // Inscription 1 - Validée
        InscriptionDTO inscription1 = InscriptionDTO.builder()
                .id(UUID.randomUUID().toString())
                .doctorantId("DOC001")
                .doctorantEmail("youssef.idrissi@edu.ma")
                .doctorantName("Youssef Idrissi")
                .directeurId("DIR001")
                .directeurName("Pr. Mohammed Benomar")
                .type(InscriptionType.INSCRIPTION_INITIALE)
                .status(InscriptionStatus.VALIDEE)
                .anneeAcademique(AnneeAcademique.ANNEE_2023_2024)
                .sujetThese("Intelligence Artificielle appliquée à la santé")
                .laboratoire("Laboratoire Informatique et Systèmes")
                .specialite("Intelligence Artificielle")
                .dateCreation(LocalDateTime.now().minusYears(2))
                .dateModification(LocalDateTime.now().minusYears(2))
                .dateValidation(LocalDateTime.now().minusYears(2).plusDays(15))
                .commentaireDirecteur("Excellent projet, sujet pertinent")
                .commentaireAdmin("Dossier complet, validé")
                .build();
        inscriptionStore.put(inscription1.getId(), inscription1);

        // Inscription 2 - En attente directeur
        InscriptionDTO inscription2 = InscriptionDTO.builder()
                .id(UUID.randomUUID().toString())
                .doctorantId("DOC002")
                .doctorantEmail("karim.alaoui@edu.ma")
                .doctorantName("Karim Alaoui")
                .directeurId("DIR001")
                .directeurName("Pr. Mohammed Benomar")
                .type(InscriptionType.INSCRIPTION_INITIALE)
                .status(InscriptionStatus.EN_ATTENTE_DIRECTEUR)
                .anneeAcademique(AnneeAcademique.ANNEE_2025_2026)
                .sujetThese("Blockchain et sécurité des données")
                .laboratoire("Laboratoire Informatique et Systèmes")
                .specialite("Sécurité Informatique")
                .dateCreation(LocalDateTime.now().minusDays(5))
                .dateModification(LocalDateTime.now().minusDays(5))
                .build();
        inscriptionStore.put(inscription2.getId(), inscription2);

        // Inscription 3 - Soumise
        InscriptionDTO inscription3 = InscriptionDTO.builder()
                .id(UUID.randomUUID().toString())
                .doctorantId("CAN001")
                .doctorantEmail("ahmed.benali@gmail.com")
                .doctorantName("Ahmed Benali")
                .directeurId("DIR001")
                .directeurName("Pr. Mohammed Benomar")
                .type(InscriptionType.INSCRIPTION_INITIALE)
                .status(InscriptionStatus.SOUMISE)
                .anneeAcademique(AnneeAcademique.ANNEE_2025_2026)
                .sujetThese("Big Data et analyse prédictive")
                .laboratoire("Laboratoire Informatique et Systèmes")
                .specialite("Big Data")
                .dateCreation(LocalDateTime.now().minusDays(2))
                .dateModification(LocalDateTime.now().minusDays(2))
                .build();
        inscriptionStore.put(inscription3.getId(), inscription3);

        log.info("{} inscriptions de test initialisées", inscriptionStore.size());
    }

    @Override
    public List<InscriptionDTO> getAllInscriptions() {
        log.info("Récupération de toutes les inscriptions");
        return new ArrayList<>(inscriptionStore.values());
    }

    @Override
    public InscriptionDTO getInscriptionById(String id) {
        log.info("Recherche de l'inscription: {}", id);
        InscriptionDTO inscription = inscriptionStore.get(id);
        if (inscription == null) {
            throw new RuntimeException("Inscription non trouvée: " + id);
        }
        return inscription;
    }

    @Override
    public InscriptionDTO createInscription(CreateInscriptionRequest request) {
        log.info("Création d'une nouvelle inscription pour: {}", request.getDoctorantId());

        InscriptionDTO inscription = InscriptionDTO.builder()
                .id(UUID.randomUUID().toString())
                .doctorantId(request.getDoctorantId())
                .doctorantEmail("doctorant@edu.ma")
                .doctorantName("Doctorant " + request.getDoctorantId())
                .directeurId(request.getDirecteurId())
                .directeurName("Directeur " + request.getDirecteurId())
                .type(request.getType())
                .status(InscriptionStatus.SOUMISE)
                .anneeAcademique(request.getAnneeAcademique())
                .sujetThese(request.getSujetThese())
                .laboratoire(request.getLaboratoire())
                .specialite(request.getSpecialite())
                .coDirecteurId(request.getCoDirecteurId())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        inscriptionStore.put(inscription.getId(), inscription);
        log.info("Inscription créée: {}", inscription.getId());
        return inscription;
    }

    @Override
    public InscriptionDTO updateInscription(String id, UpdateInscriptionRequest request) {
        log.info("Mise à jour de l'inscription: {}", id);

        InscriptionDTO inscription = getInscriptionById(id);

        if (request.getSujetThese() != null) {
            inscription.setSujetThese(request.getSujetThese());
        }
        if (request.getLaboratoire() != null) {
            inscription.setLaboratoire(request.getLaboratoire());
        }
        if (request.getSpecialite() != null) {
            inscription.setSpecialite(request.getSpecialite());
        }
        if (request.getCoDirecteurId() != null) {
            inscription.setCoDirecteurId(request.getCoDirecteurId());
        }

        inscription.setDateModification(LocalDateTime.now());
        inscriptionStore.put(id, inscription);

        log.info("Inscription mise à jour: {}", id);
        return inscription;
    }

    @Override
    public void deleteInscription(String id) {
        log.info("Suppression de l'inscription: {}", id);
        getInscriptionById(id); // Vérifier l'existence
        inscriptionStore.remove(id);
        log.info("Inscription supprimée: {}", id);
    }

    @Override
    public InscriptionDTO validateByDirecteur(String id, ValidateInscriptionRequest request) {
        log.info("Validation par directeur: {} - Approuvé: {}", id, request.isApproved());

        InscriptionDTO inscription = getInscriptionById(id);

        if (request.isApproved()) {
            inscription.setStatus(InscriptionStatus.APPROUVEE_DIRECTEUR);
            inscription.setCommentaireDirecteur(request.getCommentaire());
        } else {
            inscription.setStatus(InscriptionStatus.REJETEE);
            inscription.setCommentaireDirecteur(request.getCommentaire());
        }

        inscription.setDateModification(LocalDateTime.now());
        inscriptionStore.put(id, inscription);

        log.info("Statut mis à jour: {}", inscription.getStatus());
        return inscription;
    }

    @Override
    public InscriptionDTO validateByAdmin(String id, ValidateInscriptionRequest request) {
        log.info("Validation par admin: {} - Approuvé: {}", id, request.isApproved());

        InscriptionDTO inscription = getInscriptionById(id);

        if (request.isApproved()) {
            inscription.setStatus(InscriptionStatus.VALIDEE);
            inscription.setCommentaireAdmin(request.getCommentaire());
            inscription.setDateValidation(LocalDateTime.now());
        } else {
            inscription.setStatus(InscriptionStatus.REJETEE);
            inscription.setCommentaireAdmin(request.getCommentaire());
        }

        inscription.setDateModification(LocalDateTime.now());
        inscriptionStore.put(id, inscription);

        log.info("Statut mis à jour: {}", inscription.getStatus());
        return inscription;
    }

    @Override
    public InscriptionStatusDTO getInscriptionStatus(String id) {
        log.info("Récupération du statut: {}", id);

        InscriptionDTO inscription = getInscriptionById(id);

        return InscriptionStatusDTO.builder()
                .id(id)
                .status(inscription.getStatus())
                .message(getStatusMessage(inscription.getStatus()))
                .lastUpdate(inscription.getDateModification())
                .build();
    }

    @Override
    public List<InscriptionDTO> getInscriptionsByDoctorant(String doctorantId) {
        log.info("Recherche des inscriptions du doctorant: {}", doctorantId);

        return inscriptionStore.values().stream()
                .filter(i -> i.getDoctorantId().equals(doctorantId))
                .collect(Collectors.toList());
    }

    @Override
    public List<InscriptionDTO> getInscriptionsByStatus(InscriptionStatus status) {
        log.info("Recherche des inscriptions avec statut: {}", status);

        return inscriptionStore.values().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public InscriptionDTO createReinscription(ReinscriptionRequest request) {
        log.info("Création d'une réinscription pour: {}", request.getDoctorantId());

        // Récupérer l'inscription précédente
        InscriptionDTO previousInscription = getInscriptionById(request.getPreviousInscriptionId());

        // Créer la nouvelle inscription en reprenant les données
        InscriptionDTO reinscription = InscriptionDTO.builder()
                .id(UUID.randomUUID().toString())
                .doctorantId(request.getDoctorantId())
                .doctorantEmail(previousInscription.getDoctorantEmail())
                .doctorantName(previousInscription.getDoctorantName())
                .directeurId(previousInscription.getDirecteurId())
                .directeurName(previousInscription.getDirecteurName())
                .type(InscriptionType.REINSCRIPTION)
                .status(InscriptionStatus.SOUMISE)
                .anneeAcademique(request.getNouvelleAnnee())
                .sujetThese(request.getSujetTheseMisAJour() != null ?
                        request.getSujetTheseMisAJour() : previousInscription.getSujetThese())
                .laboratoire(previousInscription.getLaboratoire())
                .specialite(previousInscription.getSpecialite())
                .coDirecteurId(previousInscription.getCoDirecteurId())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        inscriptionStore.put(reinscription.getId(), reinscription);
        log.info("Réinscription créée: {}", reinscription.getId());
        return reinscription;
    }

    private String getStatusMessage(InscriptionStatus status) {
        switch (status) {
            case BROUILLON: return "Dossier en cours de préparation";
            case SOUMISE: return "Dossier soumis, en attente de traitement";
            case EN_ATTENTE_DIRECTEUR: return "En attente de validation du directeur de thèse";
            case APPROUVEE_DIRECTEUR: return "Approuvé par le directeur, en attente validation administrative";
            case EN_ATTENTE_ADMIN: return "En attente de validation administrative";
            case VALIDEE: return "Inscription validée";
            case REJETEE: return "Dossier rejeté";
            default: return "Statut inconnu";
        }
    }
}
