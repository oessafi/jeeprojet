package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.dto.*;
import com.devbuild.soutenanceservice.enums.DocumentType;
import com.devbuild.soutenanceservice.model.SoutenanceDocument;
import com.devbuild.soutenanceservice.services.SoutenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/soutenances")
public class SoutenanceController {

    private final SoutenanceService soutenanceService;

    /**
     * Étape 1: Le doctorant initie sa demande (formulaire)
     */
    @PostMapping
    public ResponseEntity<DemandeSoutenanceDTO> initierDemande(@RequestBody DemandeSoutenanceRequest request) {
        log.info("POST /soutenances - Initiation de la demande");
        try {
            DemandeSoutenanceDTO dto = soutenanceService.initierDemande(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            log.warn("Échec de l'initiation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeSoutenanceDTO> getDemande(@PathVariable String id) {
        log.info("GET /soutenances/{}", id);
        return ResponseEntity.ok(soutenanceService.getDemandeById(id));
    }

    /**
     * Étape 2: Le doctorant uploade les documents requis
     */
    @PostMapping("/{id}/documents")
    public ResponseEntity<DocumentInfoDTO> uploadDocument(
            @PathVariable String id,
            @RequestParam("type") DocumentType documentType,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /soutenances/{}/documents?type={} - Upload de {}", id, documentType, file.getOriginalFilename());
        try {
            DocumentInfoDTO docInfo = soutenanceService.addDocument(id, documentType, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(docInfo);
        } catch (Exception e) {
            log.error("Erreur upload document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint pour télécharger un document de soutenance
     */
    @GetMapping("/documents/{docId}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String docId) {
        log.info("GET /documents/{}", docId);
        SoutenanceDocument document = soutenanceService.getDocument(docId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getData());
    }

    /**
     * Étape 3: L'administration valide les prérequis et les documents
     */
    @PutMapping("/{id}/valider")
    // TODO: Sécuriser (Admin seulement)
    public ResponseEntity<DemandeSoutenanceDTO> validerDemande(
            @PathVariable String id,
            @RequestBody ValidationAdminRequest request) {

        log.info("PUT /soutenances/{}/valider - Validation admin: {}", id, request.isApprouvee());
        DemandeSoutenanceDTO dto = soutenanceService.validerDemandeAdmin(id, request);
        return ResponseEntity.ok(dto);
    }

    // --- NOUVEAUX ENDPOINTS ---

    /**
     * Étape 4: Le directeur de thèse propose le jury
     */
    @PostMapping("/{id}/jury")
    // TODO: Sécuriser (Directeur de thèse seulement)
    public ResponseEntity<DemandeSoutenanceDTO> proposerJury(
            @PathVariable String id,
            @RequestBody PropositionJuryRequest request) {

        log.info("POST /soutenances/{}/jury - Proposition du jury", id);
        try {
            DemandeSoutenanceDTO dto = soutenanceService.proposerJury(id, request);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            log.warn("Échec proposition jury: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Étape 5: L'administration planifie la soutenance (date/lieu)
     */
    @PutMapping("/{id}/planifier")
    // TODO: Sécuriser (Admin seulement)
    public ResponseEntity<DemandeSoutenanceDTO> planifierSoutenance(
            @PathVariable String id,
            @RequestBody PlanificationRequest request) {

        log.info("PUT /soutenances/{}/planifier - Planification de la soutenance", id);
        try {
            DemandeSoutenanceDTO dto = soutenanceService.planifierSoutenance(id, request);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            log.warn("Échec planification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}