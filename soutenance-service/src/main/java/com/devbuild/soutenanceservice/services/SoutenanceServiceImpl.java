package com.devbuild.soutenanceservice.services;

import com.devbuild.soutenanceservice.client.NotificationClient;
import com.devbuild.soutenanceservice.dto.*;
import com.devbuild.soutenanceservice.enums.DocumentType;
import com.devbuild.soutenanceservice.enums.RoleJury;
import com.devbuild.soutenanceservice.enums.SoutenanceStatus;
import com.devbuild.soutenanceservice.model.DemandeSoutenance;
import com.devbuild.soutenanceservice.model.JuryMembre;
import com.devbuild.soutenanceservice.model.SoutenanceDocument;
import com.devbuild.soutenanceservice.repository.DemandeSoutenanceRepository;
import com.devbuild.soutenanceservice.repository.JuryMembreRepository;
import com.devbuild.soutenanceservice.repository.SoutenanceDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SoutenanceServiceImpl implements SoutenanceService {

    // Prérequis (inchangés)
    private static final int REQUIS_ARTICLES_Q1Q2 = 2;
    private static final int REQUIS_CONFERENCES = 2;
    private static final int REQUIS_CREDITS_FORMATION = 200;

    private final DemandeSoutenanceRepository demandeRepository;
    private final SoutenanceDocumentRepository documentRepository;
    private final NotificationClient notificationClient;

    // --- AJOUT ---
    private final JuryMembreRepository juryMembreRepository;
    // --- FIN AJOUT ---

    // ... (initierDemande, validerDemandeAdmin, addDocument, getDocument, getDemandeById restent inchangés) ...
    @Override
    @Transactional
    import org.springframework.kafka.core.KafkaTemplate;
    public DemandeSoutenanceDTO initierDemande(DemandeSoutenanceRequest request) {
        log.info("Initiation d'une demande de soutenance pour le doctorant {}", request.getDoctorantId());

        private final KafkaTemplate<String, Object> kafkaTemplate;
        if (request.getNbArticlesQ1Q2() < REQUIS_ARTICLES_Q1Q2) {
            throw new RuntimeException("Prérequis non atteint: " + REQUIS_ARTICLES_Q1Q2 + " articles Q1/Q2 requis.");
        }
        if (request.getNbConferences() < REQUIS_CONFERENCES) {
            throw new RuntimeException("Prérequis non atteint: " + REQUIS_CONFERENCES + " conférences requises.");
        }
        if (request.getNbCreditsFormation() < REQUIS_CREDITS_FORMATION) {
            throw new RuntimeException("Prérequis non atteint: " + REQUIS_CREDITS_FORMATION + "h de formation requises.");
        }

        // 2. Créer l'entité
        DemandeSoutenance demande = DemandeSoutenance.builder()
                .doctorantId(request.getDoctorantId())
                .inscriptionId(request.getInscriptionId())
                .status(SoutenanceStatus.INITIEE)
                .nbArticlesQ1Q2(request.getNbArticlesQ1Q2())
                .nbConferences(request.getNbConferences())
                .nbCreditsFormation(request.getNbCreditsFormation())
                .preRequisValidesAdmin(false)
                .build();

        DemandeSoutenance savedDemande = demandeRepository.save(demande);
        log.info("Demande {} créée, en attente de documents.", savedDemande.getId());

        // 3. Envoyer la notification par HTTP (Feign)
        try {
            EmailRequest email = new EmailRequest(
                    "admin@univ.ma", // TODO: Récupérer l'email de l'admin
                    "Nouvelle demande de soutenance",
                    "Une nouvelle demande de soutenance a été initiée par le doctorant " + savedDemande.getDoctorantId() + ". ID Demande: " + savedDemande.getId()
            );
            notificationClient.sendNotification(email);
        } catch (Exception e) {
            log.error("Échec de l'envoi de la notification pour la demande {}: {}", savedDemande.getId(), e.getMessage());
        }

        return mapToDTO(savedDemande);
    }

    @Override
    @Transactional
    public DemandeSoutenanceDTO validerDemandeAdmin(String demandeId, ValidationAdminRequest request) {
                kafkaTemplate.send("notification-topic", email);
        DemandeSoutenance demande = getDemandeEntityById(demandeId);

        if (request.isApprouvee()) {
            demande.setStatus(SoutenanceStatus.VALIDEE_ADMIN);
            demande.setPreRequisValidesAdmin(true);
        } else {
            demande.setStatus(SoutenanceStatus.REJETEE);
            demande.setPreRequisValidesAdmin(false);
        }
        demande.setCommentaireAdmin(request.getCommentaire());

        DemandeSoutenance savedDemande = demandeRepository.save(demande);

        // Envoyer notification HTTP (Feign)
        try {
            EmailRequest email = new EmailRequest(
                    "doctorant@univ.ma", // TODO: Récupérer l'email du doctorant (via Feign UserClient)
                    "Mise à jour de votre demande de soutenance",
                    "Votre demande de soutenance " + savedDemande.getId() + " a été " + (request.isApprouvee() ? "validée" : "rejetée") + " par l'administration."
            );
            notificationClient.sendNotification(email);
        } catch (Exception e) {
            log.error("Échec de l'envoi de la notification pour la demande {}: {}", savedDemande.getId(), e.getMessage());
        }

        return mapToDTO(savedDemande);
    }

    @Override
    @Transactional
    public DocumentInfoDTO addDocument(String demandeId, DocumentType documentType, MultipartFile file) throws IOException {
                kafkaTemplate.send("notification-topic", email);
        DemandeSoutenance demande = getDemandeEntityById(demandeId);

        SoutenanceDocument document = SoutenanceDocument.builder()
                .demandeSoutenance(demande)
                .documentType(documentType)
                .fileName(StringUtils.cleanPath(file.getOriginalFilename()))
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .data(file.getBytes())
                .build();

        SoutenanceDocument savedDoc = documentRepository.save(document);

        demande.setStatus(SoutenanceStatus.PREQUIS_A_VALIDER);
        demandeRepository.save(demande);

        return mapDocumentToInfoDTO(savedDoc);
    }

    @Override
    @Transactional(readOnly = true)
    public SoutenanceDocument getDocument(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document de soutenance non trouvé: " + documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeSoutenanceDTO getDemandeById(String demandeId) {
        return mapToDTO(getDemandeEntityById(demandeId));
    }

    // --- NOUVELLES MÉTHODES ---

    @Override
    @Transactional
    public DemandeSoutenanceDTO proposerJury(String demandeId, PropositionJuryRequest request) {
        log.info("Proposition du jury pour la demande {}", demandeId);
        DemandeSoutenance demande = getDemandeEntityById(demandeId);
                kafkaTemplate.send("notification-topic", email);
        // On ne peut proposer un jury que si l'admin a validé les prérequis
        if (demande.getStatus() != SoutenanceStatus.VALIDEE_ADMIN) {
            throw new RuntimeException("La demande doit être validée par l'administration avant de proposer un jury.");
        }

        // Supprimer l'ancien jury si existant (pour mise à jour)
        juryMembreRepository.deleteAll(demande.getMembresJury());

        // Créer les nouveaux membres
        List<JuryMembre> nouveauxMembres = request.getMembresJury().stream()
                .map(dto -> mapJuryDTOToEntity(dto, demande))
                .collect(Collectors.toList());

        juryMembreRepository.saveAll(nouveauxMembres);

        demande.setStatus(SoutenanceStatus.JURY_PROPOSE);
        demande.setMembresJury(nouveauxMembres);
        DemandeSoutenance savedDemande = demandeRepository.save(demande);

        // Notifier l'admin que le jury est prêt pour validation/planification
        try {
            EmailRequest email = new EmailRequest(
                    "admin@univ.ma",
                    "Proposition de jury pour la demande " + demandeId,
                    "Le jury pour la demande " + demandeId + " a été proposé par le directeur de thèse. Veuillez procéder à la planification."
            );
            notificationClient.sendNotification(email);
        } catch (Exception e) {
            log.error("Échec notification proposition jury {}: {}", demandeId, e.getMessage());
                kafkaTemplate.send("notification-topic", new EmailRequest("doctorant@univ.ma", subject, body));

        return mapToDTO(savedDemande);
    }
                    kafkaTemplate.send("notification-topic", new EmailRequest(membre.getEmail(), subject, body));
    @Override
    @Transactional
    public DemandeSoutenanceDTO planifierSoutenance(String demandeId, PlanificationRequest request) {
        log.info("Planification de la soutenance {}", demandeId);
        DemandeSoutenance demande = getDemandeEntityById(demandeId);

        // On ne peut planifier qu'après la proposition du jury
        if (demande.getStatus() != SoutenanceStatus.JURY_PROPOSE) {
            throw new RuntimeException("Le jury doit être proposé avant la planification.");
        }

        demande.setDateSoutenance(request.getDateSoutenance());
        demande.setLieuSoutenance(request.getLieuSoutenance());
        demande.setStatus(SoutenanceStatus.PLANIFIEE);

        DemandeSoutenance savedDemande = demandeRepository.save(demande);

        // Notifier le doctorant et les membres du jury
        try {
            String subject = "Convocation soutenance de thèse";
            String body = "Votre soutenance est planifiée le " + request.getDateSoutenance() + " à " + request.getLieuSoutenance();

            // TODO: Récupérer l'email du doctorant (via UserClient) et envoyer
            notificationClient.sendNotification(new EmailRequest("doctorant@univ.ma", subject, body));

            // Envoyer aux membres du jury
            for (JuryMembre membre : savedDemande.getMembresJury()) {
                notificationClient.sendNotification(new EmailRequest(membre.getEmail(), subject, body));
            }
        } catch (Exception e) {
            log.error("Échec notification planification {}: {}", demandeId, e.getMessage());
        }

        return mapToDTO(savedDemande);
    }

    // --- FIN DES NOUVELLES MÉTHODES ---

    private DemandeSoutenance getDemandeEntityById(String demandeId) {
        return demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande de soutenance non trouvée: " + demandeId));
    }

    @Transactional(readOnly = true)
    private DemandeSoutenanceDTO mapToDTO(DemandeSoutenance demande) {
        // Mapping documents
        List<DocumentInfoDTO> documents = List.of();
        try {
            if (demande.getDocuments() != null) {
                documents = demande.getDocuments().stream()
                        .map(this::mapDocumentToInfoDTO)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Impossible de charger les documents LAZY pour la demande {}", demande.getId());
        }

        // --- AJOUT : Mapping Jury ---
        List<JuryMembreDTO> jury = List.of();
        try {
            if (demande.getMembresJury() != null) {
                jury = demande.getMembresJury().stream()
                        .map(this::mapJuryEntityToDTO)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Impossible de charger le jury LAZY pour la demande {}", demande.getId());
        }
        // --- FIN AJOUT ---

        return DemandeSoutenanceDTO.builder()
                .id(demande.getId())
                .doctorantId(demande.getDoctorantId())
                .status(demande.getStatus())
                .nbArticlesQ1Q2(demande.getNbArticlesQ1Q2())
                .nbConferences(demande.getNbConferences())
                .nbCreditsFormation(demande.getNbCreditsFormation())
                .preRequisValidesAdmin(demande.isPreRequisValidesAdmin())
                .dateCreation(demande.getDateCreation())
                .commentaireAdmin(demande.getCommentaireAdmin())
                .documents(documents)
                // --- AJOUTS ---
                .membresJury(jury)
                .dateSoutenance(demande.getDateSoutenance())
                .lieuSoutenance(demande.getLieuSoutenance())
                // --- FIN AJOUTS ---
                .build();
    }

    private DocumentInfoDTO mapDocumentToInfoDTO(SoutenanceDocument document) {
        return DocumentInfoDTO.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .build();
    }

    // --- NOUVEAUX HELPERS ---
    private JuryMembreDTO mapJuryEntityToDTO(JuryMembre membre) {
        JuryMembreDTO dto = new JuryMembreDTO();
        dto.setId(membre.getId());
        dto.setNomComplet(membre.getNomComplet());
        dto.setEmail(membre.getEmail());
        dto.setEtablissement(membre.getEtablissement());
        dto.setRole(membre.getRole());
        return dto;
    }

    private JuryMembre mapJuryDTOToEntity(JuryMembreDTO dto, DemandeSoutenance demande) {
        return JuryMembre.builder()
                .nomComplet(dto.getNomComplet())
                .email(dto.getEmail())
                .etablissement(dto.getEtablissement())
                .role(dto.getRole())
                .demandeSoutenance(demande)
                .build();
    }
}