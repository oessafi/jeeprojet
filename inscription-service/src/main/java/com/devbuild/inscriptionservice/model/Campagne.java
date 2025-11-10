package com.devbuild.inscriptionservice.model;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campagnes")
public class Campagne {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnneeAcademique anneeAcademique;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InscriptionType type; // INSCRIPTION_INITIALE ou REINSCRIPTION

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Builder.Default
    private boolean active = false; // "ouverte" ou "fermée"

    @CreationTimestamp
    private LocalDateTime dateCreation;
}