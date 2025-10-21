package com.devbuild.inscriptionservice.dto;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinscriptionRequest {

    private String doctorantId;
    private String previousInscriptionId;
    private AnneeAcademique nouvelleAnnee;
    private String sujetTheseMisAJour;
}