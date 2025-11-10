package com.devbuild.soutenanceservice.services;

import com.devbuild.soutenanceservice.dto.*; // Import de tous les DTOs
import com.devbuild.soutenanceservice.enums.DocumentType;
import com.devbuild.soutenanceservice.model.SoutenanceDocument;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface SoutenanceService {

    DemandeSoutenanceDTO initierDemande(DemandeSoutenanceRequest request);

    DemandeSoutenanceDTO validerDemandeAdmin(String demandeId, ValidationAdminRequest request);

    DocumentInfoDTO addDocument(String demandeId, DocumentType documentType, MultipartFile file) throws IOException;

    SoutenanceDocument getDocument(String documentId);

    DemandeSoutenanceDTO getDemandeById(String demandeId);


    DemandeSoutenanceDTO proposerJury(String demandeId, PropositionJuryRequest request);

    DemandeSoutenanceDTO planifierSoutenance(String demandeId, PlanificationRequest request);

}