package com.devbuild.inscriptionservice.controller;

import com.devbuild.inscriptionservice.dto.DocumentInfoDTO;
import com.devbuild.inscriptionservice.model.Document;
import com.devbuild.inscriptionservice.services.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Uploade un document lié à une inscription.
     * C'est l'endpoint que le frontend appellera.
     */
    @PostMapping("/inscriptions/{id}/documents")
    // TODO: Sécuriser cet endpoint (Propriétaire ou Admin)
    public ResponseEntity<DocumentInfoDTO> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /inscriptions/{}/documents - Upload de {}", id, file.getOriginalFilename());
        try {
            DocumentInfoDTO docInfo = documentService.store(file, id);
            return ResponseEntity.status(HttpStatus.CREATED).body(docInfo);
        } catch (IOException e) {
            log.error("Erreur lors de l'upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Télécharge un document par son ID.
     */
    @GetMapping("/documents/{docId}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String docId) {
        log.info("GET /documents/{}", docId);
        Document document = documentService.getDocument(docId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getData());
    }

    /**
     * Supprime un document par son ID.
     */
    @DeleteMapping("/documents/{docId}")
    // TODO: Sécuriser cet endpoint
    public ResponseEntity<Void> deleteDocument(@PathVariable String docId) {
        log.info("DELETE /documents/{}", docId);
        documentService.deleteDocument(docId);
        return ResponseEntity.ok().build();
    }
}