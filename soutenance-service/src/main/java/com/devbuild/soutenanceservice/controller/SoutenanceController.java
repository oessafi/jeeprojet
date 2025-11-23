package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.dto.*;
import com.devbuild.soutenanceservice.enums.DocumentType;
import com.devbuild.soutenanceservice.model.DemandeSoutenance;
import com.devbuild.soutenanceservice.model.SoutenanceDocument;
import com.devbuild.soutenanceservice.repository.DemandeSoutenanceRepository;
import com.devbuild.soutenanceservice.services.SoutenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/soutenances")
public class SoutenanceController {

    private final SoutenanceService soutenanceService;
    // Injection directe du repository pour la lecture simple (ou passer par le service)
    private final DemandeSoutenanceRepository demandeRepository;

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

    // --- NOUVEL ENDPOINT POUR FEIGN (USER-SERVICE) ---
    @GetMapping("/doctorant/{doctorantId}")
    public ResponseEntity<SoutenanceListResponse> getSoutenancesByDoctorant(@PathVariable String doctorantId) {
        log.info("GET /soutenances/doctorant/{}", doctorantId);

        // Récupération via Repository (plus direct pour une liste simple)
        List<DemandeSoutenance> demandes = demandeRepository.findByDoctorantId(doctorantId);

        // Conversion en DTOs
        // Note: Idéalement, exposez une méthode mapToDTO publique dans le Service ou un Mapper dédié
        // Ici, nous utilisons le service pour récupérer le DTO unitaire pour simplifier
        List<DemandeSoutenanceDTO> dtos = demandes.stream()
                .map(d -> soutenanceService.getDemandeById(d.getId()))
                .collect(Collectors.toList());

        // Construction de la réponse
        // Assurez-vous d'avoir créé la classe SoutenanceListResponse dans le package dto
        SoutenanceListResponse response = SoutenanceListResponse.builder()
                .success(true)
                .message("Soutenances du doctorant récupérées")
                .data(dtos)
                .total(dtos.size())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
    // ------------------------------------------------

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

    @GetMapping("/documents/{docId}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String docId) {
        log.info("GET /documents/{}", docId);
        SoutenanceDocument document = soutenanceService.getDocument(docId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getData());
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<DemandeSoutenanceDTO> validerDemande(
            @PathVariable String id,
            @RequestBody ValidationAdminRequest request) {

        log.info("PUT /soutenances/{}/valider - Validation admin: {}", id, request.isApprouvee());
        DemandeSoutenanceDTO dto = soutenanceService.validerDemandeAdmin(id, request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/jury")
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

    @PutMapping("/{id}/planifier")
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