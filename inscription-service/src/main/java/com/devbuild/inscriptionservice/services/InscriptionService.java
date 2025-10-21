package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.dto.*;
import com.devbuild.inscriptionservice.enums.InscriptionStatus;

import java.util.List;

public interface InscriptionService {

    List<InscriptionDTO> getAllInscriptions();
    InscriptionDTO getInscriptionById(String id);
    InscriptionDTO createInscription(CreateInscriptionRequest request);
    InscriptionDTO updateInscription(String id, UpdateInscriptionRequest request);
    void deleteInscription(String id);
    InscriptionDTO validateByDirecteur(String id, ValidateInscriptionRequest request);
    InscriptionDTO validateByAdmin(String id, ValidateInscriptionRequest request);
    InscriptionStatusDTO getInscriptionStatus(String id);
    List<InscriptionDTO> getInscriptionsByDoctorant(String doctorantId);
    List<InscriptionDTO> getInscriptionsByStatus(InscriptionStatus status);
    InscriptionDTO createReinscription(ReinscriptionRequest request);
}