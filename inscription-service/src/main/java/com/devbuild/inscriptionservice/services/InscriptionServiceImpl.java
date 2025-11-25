package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.client.UserClient;
import com.devbuild.inscriptionservice.dto.user.UserDTO;
import com.devbuild.inscriptionservice.dto.user.UserResponseWrapper;
import com.devbuild.inscriptionservice.model.Inscription;
import com.devbuild.inscriptionservice.repository.InscriptionRepository;
import com.devbuild.inscriptionservice.repository.CampagneRepository;
import com.devbuild.inscriptionservice.model.Document;
import com.devbuild.inscriptionservice.dto.DocumentInfoDTO;

// Imports originaux
import com.devbuild.inscriptionservice.dto.*;
import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionStatus;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- CORRECTION ICI

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// AJOUT DES IMPORTS POUR RESILIENCE4J
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
@Slf4j
@RequiredArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    // 1. Déclaration des dépendances
    private final UserClient userClient;
    private final InscriptionRepository inscriptionRepository;
    private final CampagneRepository campagneRepository; // <-- AJOUT

    // Le constructeur est maintenant géré par @RequiredArgsConstructor

    @Override
    @Transactional(readOnly = true) // <-- ANNOTATION TRANSACTIONNELLE
    public List<InscriptionDTO> getAllInscriptions() {
        log.info("Récupération de toutes les inscriptions depuis la BDD");
        return inscriptionRepository.findAll()
                .stream()
                .map(this::mapToDTO) // Convertir Entité en DTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // <-- ANNOTATION TRANSACTIONNELLE
    public InscriptionDTO getInscriptionById(String id) {
        log.info("Recherche de l'inscription: {}", id);
        Inscription inscription = getInscriptionEntityById(id);
        return mapToDTO(inscription);
    }

    // Méthode privée pour récupérer l'ENTITÉ (utile pour les mises à jour)
    private Inscription getInscriptionEntityById(String id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée: " + id));
    }

    /**
     * AJOUT D'UNE MÉTHODE DE VÉRIFICATION
     * Vérifie si une campagne est active pour le type d'inscription demandé.
     * Lève une exception si aucune campagne n'est trouvée.
     */
    private void checkActiveCampaign(InscriptionType type) {
        log.info("Vérification de campagne active pour le type: {}", type);
        campagneRepository.findActiveCampaign(type, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Aucune campagne active n'est disponible pour " + type));
    }


    @Override
    @Transactional // <-- ANNOTATION TRANSACTIONNELLE
    // AJOUT : Annotations Resilience4J
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "fallbackCreateInscription")
    @Retry(name = "userServiceRetry")
    public InscriptionDTO createInscription(CreateInscriptionRequest request) {
        // VÉRIFICATION DE LA CAMPAGNE
        checkActiveCampaign(request.getType());

        log.info("Création d'une nouvelle inscription pour: {}", request.getDoctorantId());

        // --- Logique Feign (inchangée) ---
        String doctorantName = "Doctorant (non trouvé)";
        String doctorantEmail = "email@inconnu.ma";
        try {
            UserResponseWrapper userResponse = userClient.getUserById(request.getDoctorantId());
            if (userResponse != null && userResponse.isSuccess()) {
                UserDTO user = userResponse.getData();
                doctorantName = user.getFirstName() + " " + user.getLastName();
                doctorantEmail = user.getEmail();
                log.info("Informations utilisateur récupérées via Feign: {}", doctorantName);
            } else {
                log.warn("Réponse invalide du user-service pour l'ID: {}", request.getDoctorantId());
            }
        } catch (Exception e) {
            log.error("Impossible de récupérer l'utilisateur via Feign: {}", e.getMessage());
            // Important : Relancer l'exception pour que Retry et CircuitBreaker la détectent
            throw new RuntimeException("User-service inaccessible: " + e.getMessage(), e);
        }
        // --- Fin Feign ---

        // Construire l'ENTITÉ Inscription
        Inscription newInscription = Inscription.builder()
                .id(UUID.randomUUID().toString()) // JPA peut le générer si vous préférez
                .doctorantId(request.getDoctorantId())
                .doctorantEmail(doctorantEmail)
                .doctorantName(doctorantName)
                .directeurId(request.getDirecteurId())
                .directeurName("Directeur " + request.getDirecteurId()) // TODO: Appeler Feign pour le directeur
                .type(request.getType())
                .status(InscriptionStatus.SOUMISE)
                .anneeAcademique(request.getAnneeAcademique())
                .sujetThese(request.getSujetThese())
                .laboratoire(request.getLaboratoire())
                .specialite(request.getSpecialite())
                .coDirecteurId(request.getCoDirecteurId())
                // dateCreation et dateModification gérées par @...Timestamp
                .build();

        // Sauvegarder l'ENTITÉ dans la BDD
        Inscription savedInscription = inscriptionRepository.save(newInscription);

        log.info("Inscription créée: {}", savedInscription.getId());
        return mapToDTO(savedInscription); // Retourner le DTO
    }

    /**
     * AJOUT : Méthode de Fallback pour createInscription.
     * Appelée par Resilience4J lorsque le Circuit Breaker est ouvert.
     *
     * @param request La requête originale
     * @param ex      L'exception qui a causé la panne
     * @return Un DTO d'inscription avec des valeurs par défaut
     */
    public InscriptionDTO fallbackCreateInscription(CreateInscriptionRequest request, Exception ex) {
        log.warn("Circuit Breaker OPEN - Fallback activé pour createInscription. Cause: {}", ex.getMessage());

        // Créer une réponse partielle sans appeler le user-service
        Inscription newInscription = Inscription.builder()
                .id(UUID.randomUUID().toString())
                .doctorantId(request.getDoctorantId())
                .doctorantEmail("email@inconnu.ma") // Valeur par défaut
                .doctorantName("Doctorant (Service indisponible)") // Valeur par défaut
                .directeurId(request.getDirecteurId())
                .directeurName("Directeur (Service indisponible)") // Valeur par défaut
                .type(request.getType())
                .status(InscriptionStatus.SOUMISE) // Ou un statut "EN_ATTENTE_VALIDATION_MANUELLE"
                .anneeAcademique(request.getAnneeAcademique())
                .sujetThese(request.getSujetThese())
                .laboratoire(request.getLaboratoire())
                .specialite(request.getSpecialite())
                .coDirecteurId(request.getCoDirecteurId())
                .build();

        Inscription savedInscription = inscriptionRepository.save(newInscription);
        log.warn("Inscription {} créée en mode fallback (dégradé).", savedInscription.getId());

        return mapToDTO(savedInscription);
    }


    @Override
    @Transactional // <-- ANNOTATION TRANSACTIONNELLE
    public InscriptionDTO updateInscription(String id, UpdateInscriptionRequest request) {
        log.info("Mise à jour de l'inscription: {}", id);

        Inscription inscription = getInscriptionEntityById(id); // Récupère l'entité

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
        // dateModification sera mise à jour automatiquement par @UpdateTimestamp

        Inscription updatedInscription = inscriptionRepository.save(inscription);

        log.info("Inscription mise à jour: {}", id);
        return mapToDTO(updatedInscription);
    }

    @Override
    @Transactional // <-- ANNOTATION TRANSACTIONNELLE
    public void deleteInscription(String id) {
        log.info("Suppression de l'inscription: {}", id);
        if (!inscriptionRepository.existsById(id)) {
            throw new RuntimeException("Inscription non trouvée: " + id);
        }
        inscriptionRepository.deleteById(id);
        log.info("Inscription supprimée: {}", id);
    }

    @Override
    @Transactional // <-- ANNOTATION TRANSACTIONNELLE
    public InscriptionDTO validateByDirecteur(String id, ValidateInscriptionRequest request) {
        log.info("Validation par directeur: {} - Approuvé: {}", id, request.isApproved());

        Inscription inscription = getInscriptionEntityById(id);

        if (request.isApproved()) {
            inscription.setStatus(InscriptionStatus.APPROUVEE_DIRECTEUR);
            inscription.setCommentaireDirecteur(request.getCommentaire());
        } else {
            inscription.setStatus(InscriptionStatus.REJETEE);
            inscription.setCommentaireDirecteur(request.getCommentaire());
        }

        Inscription updatedInscription = inscriptionRepository.save(inscription);
        log.info("Statut mis à jour: {}", updatedInscription.getStatus());
        return mapToDTO(updatedInscription);
    }

    @Override
    @Transactional // <-- ANNOTATION TRANSACTIONNELLE
    public InscriptionDTO validateByAdmin(String id, ValidateInscriptionRequest request) {
        log.info("Validation par admin: {} - Approuvé: {}", id, request.isApproved());

        Inscription inscription = getInscriptionEntityById(id);

        if (request.isApproved()) {
            inscription.setStatus(InscriptionStatus.VALIDEE);
            inscription.setCommentaireAdmin(request.getCommentaire());
            inscription.setDateValidation(LocalDateTime.now());
        } else {
            inscription.setStatus(InscriptionStatus.REJETEE);
            inscription.setCommentaireAdmin(request.getCommentaire());
        }

        Inscription updatedInscription = inscriptionRepository.save(inscription);
        log.info("Statut mis à jour: {}", updatedInscription.getStatus());
        return mapToDTO(updatedInscription);
    }

    @Override
    @Transactional(readOnly = true) // <-- ANNOTATION TRANSACTIONNELLE
    public InscriptionStatusDTO getInscriptionStatus(String id) {
        log.info("Récupération du statut: {}", id);
        Inscription inscription = getInscriptionEntityById(id);

        return InscriptionStatusDTO.builder()
                .id(id)
                .status(inscription.getStatus())
                .message(getStatusMessage(inscription.getStatus()))
                .lastUpdate(inscription.getDateModification())
                .build();
    }

    @Override
    @Transactional(readOnly = true) // <-- ANNOTATION TRANSACTIONNELLE
    public List<InscriptionDTO> getInscriptionsByDoctorant(String doctorantId) {
        log.info("Recherche des inscriptions du doctorant: {}", doctorantId);
        // Utilise la méthode du repository
        return inscriptionRepository.findByDoctorantId(doctorantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // <-- ANNOTATION TRANSACTIONNELLE
    public List<InscriptionDTO> getInscriptionsByStatus(InscriptionStatus status) {
        log.info("Recherche des inscriptions avec statut: {}", status);
        // Utilise la méthode du repository
        return inscriptionRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // <-- ANNOTATION TRANSACTIONNELLE
    public InscriptionDTO createReinscription(ReinscriptionRequest request) {
        // VÉRIFICATION DE LA CAMPAGNE
        checkActiveCampaign(InscriptionType.REINSCRIPTION);

        log.info("Création d'une réinscription pour: {}", request.getDoctorantId());

        Inscription previousInscription = getInscriptionEntityById(request.getPreviousInscriptionId());

        Inscription reinscription = Inscription.builder()
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
                .build();

        Inscription savedReinscription = inscriptionRepository.save(reinscription);
        log.info("Réinscription créée: {}", savedReinscription.getId());
        return mapToDTO(savedReinscription);
    }

    // --- Méthodes privées (Helpers) ---

    // Convertit une Entité Inscription en InscriptionDTO
    private InscriptionDTO mapToDTO(Inscription inscription) {

        List<DocumentInfoDTO> documentInfos = new ArrayList<>();
        // Cette ligne est maintenant sûre car la méthode appelante est @Transactional
        try {
            if (inscription.getDocuments() != null) {
                documentInfos = inscription.getDocuments().stream()
                        .map(doc -> DocumentInfoDTO.builder()
                                .id(doc.getId())
                                .fileName(doc.getFileName())
                                .contentType(doc.getContentType())
                                .fileSize(doc.getFileSize())
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Impossible de charger la liste de documents pour l'inscription {}: {}", inscription.getId(), e.getMessage());
        }

        return InscriptionDTO.builder()
                .id(inscription.getId())
                .doctorantId(inscription.getDoctorantId())
                .doctorantEmail(inscription.getDoctorantEmail())
                .doctorantName(inscription.getDoctorantName())
                .directeurId(inscription.getDirecteurId())
                .directeurName(inscription.getDirecteurName())
                .type(inscription.getType())
                .status(inscription.getStatus())
                .anneeAcademique(inscription.getAnneeAcademique())
                .sujetThese(inscription.getSujetThese())
                .laboratoire(inscription.getLaboratoire())
                .specialite(inscription.getSpecialite())
                .coDirecteurId(inscription.getCoDirecteurId())
                .coDirecteurName(inscription.getCoDirecteurName())
                .documents(documentInfos) // <-- Champ mis à jour
                .commentaireDirecteur(inscription.getCommentaireDirecteur())
                .commentaireAdmin(inscription.getCommentaireAdmin())
                .dateCreation(inscription.getDateCreation())
                .dateModification(inscription.getDateModification())
                .dateValidation(inscription.getDateValidation())
                .build();
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