package com.devbuild.soutenanceservice.services;

import com.devbuild.soutenanceservice.dto.*;
import com.devbuild.soutenanceservice.enums.DocumentType;
import com.devbuild.soutenanceservice.enums.SoutenanceStatus;
import com.devbuild.soutenanceservice.model.DemandeSoutenance;
import com.devbuild.soutenanceservice.model.JuryMembre;
import com.devbuild.soutenanceservice.model.SoutenanceDocument;
import com.devbuild.soutenanceservice.repository.DemandeSoutenanceRepository;
import com.devbuild.soutenanceservice.repository.JuryMembreRepository;
import com.devbuild.soutenanceservice.repository.SoutenanceDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate; // Import correct
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

    private static final int REQUIS_ARTICLES_Q1Q2 = 2;
    private static final int REQUIS_CONFERENCES = 2;
    private static final int REQUIS_CREDITS_FORMATION = 200;
    private static final String NOTIFICATION_TOPIC = "notification-topic";

    private final DemandeSoutenanceRepository demandeRepository;
    private final SoutenanceDocumentRepository documentRepository;
    private final JuryMembreRepository juryMembreRepository;

    // Injection Kafka correcte via Lombok
    private final KafkaTemplate<String, EmailRequest> kafkaTemplate;

    @Override
    @Transactional
    public DemandeSoutenanceDTO initierDemande(DemandeSoutenanceRequest request) {
        log.info("Initiation demande pour doctorant {}", request.getDoctorantId());

        if (request.getNbArticlesQ1Q2() < REQUIS_ARTICLES_Q1Q2) throw new RuntimeException("Manque articles Q1/Q2");
        if (request.getNbConferences() < REQUIS_CONFERENCES) throw new RuntimeException("Manque conférences");
        if (request.getNbCreditsFormation() < REQUIS_CREDITS_FORMATION) throw new RuntimeException("Manque crédits");

        DemandeSoutenance demande = DemandeSoutenance.builder()
                .doctorantId(request.getDoctorantId())
                .inscriptionId(request.getInscriptionId())
                .status(SoutenanceStatus.INITIEE)
                .nbArticlesQ1Q2(request.getNbArticlesQ1Q2())
                .nbConferences(request.getNbConferences())
                .nbCreditsFormation(request.getNbCreditsFormation())
                .preRequisValidesAdmin(false)
                .build();

        DemandeSoutenance saved = demandeRepository.save(demande);

        // Notification Kafka
        EmailRequest email = new EmailRequest("admin@univ.ma", "Nouvelle demande", "ID: " + saved.getId());
        kafkaTemplate.send(NOTIFICATION_TOPIC, email);

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public DemandeSoutenanceDTO validerDemandeAdmin(String demandeId, ValidationAdminRequest request) {
        DemandeSoutenance demande = getDemandeEntityById(demandeId);

        demande.setStatus(request.isApprouvee() ? SoutenanceStatus.VALIDEE_ADMIN : SoutenanceStatus.REJETEE);
        demande.setPreRequisValidesAdmin(request.isApprouvee());
        demande.setCommentaireAdmin(request.getCommentaire());

        DemandeSoutenance saved = demandeRepository.save(demande);

        // Notification Kafka
        EmailRequest email = new EmailRequest("doctorant@univ.ma", "Validation Admin", "Statut: " + saved.getStatus());
        kafkaTemplate.send(NOTIFICATION_TOPIC, email);

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public DemandeSoutenanceDTO proposerJury(String demandeId, PropositionJuryRequest request) {
        DemandeSoutenance demande = getDemandeEntityById(demandeId);
        if (demande.getStatus() != SoutenanceStatus.VALIDEE_ADMIN) {
            throw new RuntimeException("Validation admin requise avant jury.");
        }

        juryMembreRepository.deleteAll(demande.getMembresJury());
        List<JuryMembre> membres = request.getMembresJury().stream()
                .map(dto -> mapJuryDTOToEntity(dto, demande))
                .collect(Collectors.toList());

        juryMembreRepository.saveAll(membres);
        demande.setStatus(SoutenanceStatus.JURY_PROPOSE);
        demande.setMembresJury(membres);

        DemandeSoutenance saved = demandeRepository.save(demande);

        // Notification Kafka
        kafkaTemplate.send(NOTIFICATION_TOPIC, new EmailRequest("admin@univ.ma", "Jury Proposé", "Pour demande " + demandeId));

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public DemandeSoutenanceDTO planifierSoutenance(String demandeId, PlanificationRequest request) {
        DemandeSoutenance demande = getDemandeEntityById(demandeId);
        if (demande.getStatus() != SoutenanceStatus.JURY_PROPOSE) {
            throw new RuntimeException("Jury doit être proposé avant planification.");
        }

        demande.setDateSoutenance(request.getDateSoutenance());
        demande.setLieuSoutenance(request.getLieuSoutenance());
        demande.setStatus(SoutenanceStatus.PLANIFIEE);

        DemandeSoutenance saved = demandeRepository.save(demande);

        // Notifications Kafka
        String subject = "Soutenance Planifiée";
        String body = "Date: " + request.getDateSoutenance();
        kafkaTemplate.send(NOTIFICATION_TOPIC, new EmailRequest("doctorant@univ.ma", subject, body));

        for (JuryMembre membre : saved.getMembresJury()) {
            kafkaTemplate.send(NOTIFICATION_TOPIC, new EmailRequest(membre.getEmail(), subject, body));
        }

        return mapToDTO(saved);
    }

    // ... (Gardez vos méthodes existantes: addDocument, getDocument, Helpers mapToDTO, etc.)
    // Assurez-vous d'ajouter les méthodes manquantes que j'ai omises par brièveté (addDocument, getDocument, etc.)
    // en copiant celles de votre ancien fichier, car elles étaient correctes.

    private DemandeSoutenance getDemandeEntityById(String id) {
        return demandeRepository.findById(id).orElseThrow(() -> new RuntimeException("Non trouvé"));
    }

    // ... (Ajoutez les autres méthodes helpers ici)
    private DemandeSoutenanceDTO mapToDTO(DemandeSoutenance demande) {
        // ... (Votre code de mapping existant)
        return DemandeSoutenanceDTO.builder().id(demande.getId()).status(demande.getStatus()).build(); // Exemple simplifié
    }

    private JuryMembre mapJuryDTOToEntity(JuryMembreDTO dto, DemandeSoutenance demande) {
        return JuryMembre.builder().nomComplet(dto.getNomComplet()).email(dto.getEmail()).role(dto.getRole()).demandeSoutenance(demande).build();
    }

    private DocumentInfoDTO mapDocumentToInfoDTO(SoutenanceDocument doc) {
        return DocumentInfoDTO.builder().id(doc.getId()).fileName(doc.getFileName()).build();
    }

    @Override
    public DocumentInfoDTO addDocument(String demandeId, DocumentType type, MultipartFile file) throws IOException {
        // ... (Code existant)
        return null;
    }

    @Override
    public SoutenanceDocument getDocument(String docId) {
        // ... (Code existant)
        return null;
    }

    @Override
    public DemandeSoutenanceDTO getDemandeById(String id) {
        // ... (Code existant)
        return null;
    }
}