package com.devbuild.inscriptionservice.controller;

import com.devbuild.inscriptionservice.dto.*;
import com.devbuild.inscriptionservice.enums.InscriptionStatus;
import com.devbuild.inscriptionservice.services.InscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/inscriptions")
public class InscriptionController {
    private final InscriptionService inscriptionService;

    @GetMapping
    public ResponseEntity<InscriptionListResponse> getAllInscriptions() {
        log.info("GET /inscriptions");

        List<InscriptionDTO> inscriptions = inscriptionService.getAllInscriptions();
        InscriptionListResponse response = InscriptionListResponse.builder()
                .success(true)
                .message("Liste des inscriptions")
                .data(inscriptions)
                .total(inscriptions.size())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscriptionResponse> getInscriptionById(@PathVariable String id) {
        log.info("GET /inscriptions/{}", id);

        InscriptionDTO inscription = inscriptionService.getInscriptionById(id);
        InscriptionResponse response = InscriptionResponse.builder()
                .success(true)
                .message("Inscription récupérée")
                .data(inscription)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<InscriptionResponse> createInscription(
            @RequestBody CreateInscriptionRequest request) {

        log.info("POST /inscriptions");

        InscriptionDTO inscription = inscriptionService.createInscription(request);
        InscriptionResponse response = InscriptionResponse.builder()
                .success(true)
                .message("Inscription créée avec succès")
                .data(inscription)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<InscriptionResponse> updateInscription(
            @PathVariable String id,
            @RequestBody UpdateInscriptionRequest request) {

        log.info("PUT /inscriptions/{}", id);

        InscriptionDTO inscription = inscriptionService.updateInscription(id, request);
        InscriptionResponse response = InscriptionResponse.builder()
                .success(true)
                .message("Inscription mise à jour")
                .data(inscription)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteInscription(@PathVariable String id) {
        log.info("DELETE /inscriptions/{}", id);

        inscriptionService.deleteInscription(id);
        MessageResponse response = MessageResponse.builder()
                .success(true)
                .message("Inscription supprimée")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/validate/directeur")
    public ResponseEntity<InscriptionResponse> validateByDirecteur(
            @PathVariable String id,
            @RequestBody ValidateInscriptionRequest request) {

        log.info("PUT /inscriptions/{}/validate/directeur", id);

        InscriptionDTO inscription = inscriptionService.validateByDirecteur(id, request);
        InscriptionResponse response = InscriptionResponse.builder()
                .success(true)
                .message("Validation directeur effectuée")
                .data(inscription)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/validate/admin")
    public ResponseEntity<InscriptionResponse> validateByAdmin(
            @PathVariable String id,
            @RequestBody ValidateInscriptionRequest request) {

        log.info("PUT /inscriptions/{}/validate/admin", id);

        InscriptionDTO inscription = inscriptionService.validateByAdmin(id, request);
        InscriptionResponse response = InscriptionResponse.builder()
                .success(true)
                .message("Validation administrative effectuée")
                .data(inscription)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<InscriptionStatusDTO> getStatus(@PathVariable String id) {
        log.info("GET /inscriptions/{}/status", id);

        InscriptionStatusDTO status = inscriptionService.getInscriptionStatus(id);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/doctorant/{doctorantId}")
    public ResponseEntity<InscriptionListResponse> getByDoctorant(@PathVariable String doctorantId) {
        log.info("GET /inscriptions/doctorant/{}", doctorantId);

        List<InscriptionDTO> inscriptions = inscriptionService.getInscriptionsByDoctorant(doctorantId);
        InscriptionListResponse response = InscriptionListResponse.builder()
                .success(true)
                .message("Inscriptions du doctorant")
                .data(inscriptions)
                .total(inscriptions.size())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<InscriptionListResponse> getByStatus(@PathVariable InscriptionStatus status) {
        log.info("GET /inscriptions/status/{}", status);

        List<InscriptionDTO> inscriptions = inscriptionService.getInscriptionsByStatus(status);
        InscriptionListResponse response = InscriptionListResponse.builder()
                .success(true)
                .message("Inscriptions avec statut: " + status)
                .data(inscriptions)
                .total(inscriptions.size())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reinscriptions")
    public ResponseEntity<InscriptionResponse> createReinscription(
            @RequestBody ReinscriptionRequest request) {

        log.info("POST /reinscriptions");

        InscriptionDTO inscription = inscriptionService.createReinscription(request);
        InscriptionResponse response = InscriptionResponse.builder()
                .success(true)
                .message("Réinscription créée avec succès")
                .data(inscription)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        log.info("GET /inscriptions/health");

        MessageResponse response = MessageResponse.builder()
                .success(true)
                .message("Inscription Service is UP")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
