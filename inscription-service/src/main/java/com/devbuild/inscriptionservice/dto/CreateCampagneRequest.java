package com.devbuild.inscriptionservice.dto;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampagneRequest {
    private AnneeAcademique anneeAcademique;
    private InscriptionType type;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}