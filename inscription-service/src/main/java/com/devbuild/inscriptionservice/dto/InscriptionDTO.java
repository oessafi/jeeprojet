package com.devbuild.inscriptionservice.dto;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionStatus;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionDTO {

    private String id;
    private String doctorantId;
    private String doctorantEmail;
    private String doctorantName;
    private String directeurId;
    private String directeurName;

    private InscriptionType type;
    private InscriptionStatus status;
    private AnneeAcademique anneeAcademique;

    // Informations académiques
    private String sujetThese;
    private String laboratoire;
    private String specialite;
    private String coDirecteurId;
    private String coDirecteurName;

    // Documents
    private List<String> documentsIds;

    // Workflow
    private String commentaireDirecteur;
    private String commentaireAdmin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateModification;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateValidation;

}