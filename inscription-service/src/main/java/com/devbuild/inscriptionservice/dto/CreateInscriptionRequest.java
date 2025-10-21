package com.devbuild.inscriptionservice.dto;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInscriptionRequest {

    private String doctorantId;
    private String directeurId;
    private InscriptionType type;
    private AnneeAcademique anneeAcademique;

    private String sujetThese;
    private String laboratoire;
    private String specialite;
    private String coDirecteurId;
}