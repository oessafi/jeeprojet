package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.dto.DocumentInfoDTO;
import com.devbuild.inscriptionservice.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {
    DocumentInfoDTO store(MultipartFile file, String inscriptionId) throws IOException;
    Document getDocument(String documentId);
    void deleteDocument(String documentId);
}