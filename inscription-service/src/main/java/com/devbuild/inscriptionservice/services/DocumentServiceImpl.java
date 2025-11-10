package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.dto.DocumentInfoDTO;
import com.devbuild.inscriptionservice.model.Document;
import com.devbuild.inscriptionservice.model.Inscription;
import com.devbuild.inscriptionservice.repository.DocumentRepository;
import com.devbuild.inscriptionservice.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final InscriptionRepository inscriptionRepository; // Pour lier le document

    @Override
    @Transactional
    public DocumentInfoDTO store(MultipartFile file, String inscriptionId) throws IOException {
        log.info("Stockage du fichier {} pour l'inscription {}", file.getOriginalFilename(), inscriptionId);

        // 1. Récupérer l'entité Inscription
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée: " + inscriptionId));

        // 2. Créer l'entité Document
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Document document = Document.builder()
                .fileName(fileName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .data(file.getBytes())
                .inscription(inscription) // Lier le document à l'inscription
                .build();

        // 3. Sauvegarder et retourner le DTO
        Document savedDocument = documentRepository.save(document);

        return DocumentInfoDTO.builder()
                .id(savedDocument.getId())
                .fileName(savedDocument.getFileName())
                .contentType(savedDocument.getContentType())
                .fileSize(savedDocument.getFileSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true) // Important pour les données LAZY (les bytes)
    public Document getDocument(String documentId) {
        log.info("Récupération du document: {}", documentId);
        // Récupère l'entité complète (y compris les données LAZY)
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé: " + documentId));
    }

    @Override
    @Transactional
    public void deleteDocument(String documentId) {
        log.info("Suppression du document: {}", documentId);
        if (!documentRepository.existsById(documentId)) {
            throw new RuntimeException("Document non trouvé: " + documentId);
        }
        documentRepository.deleteById(documentId);
    }
}