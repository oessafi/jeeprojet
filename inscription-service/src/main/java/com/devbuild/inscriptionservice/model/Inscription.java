package com.devbuild.inscriptionservice.model;

import com.devbuild.inscriptionservice.enums.AnneeAcademique;
import com.devbuild.inscriptionservice.enums.InscriptionStatus;
import com.devbuild.inscriptionservice.enums.InscriptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List; // <-- AJOUT

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inscriptions")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String doctorantId;

    private String doctorantEmail;
    private String doctorantName;

    @Column(nullable = false)
    private String directeurId;
    private String directeurName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InscriptionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InscriptionStatus status;

    @Enumerated(EnumType.STRING)
    private AnneeAcademique anneeAcademique;

    // Informations académiques
    @Column(columnDefinition = "TEXT")
    private String sujetThese;
    private String laboratoire;
    private String specialite;
    private String coDirecteurId;
    private String coDirecteurName;

    // Workflow
    @Column(columnDefinition = "TEXT")
    private String commentaireDirecteur;
    @Column(columnDefinition = "TEXT")
    private String commentaireAdmin;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    private LocalDateTime dateValidation;

    // --- AJOUT DE LA RELATION INVERSE ---
    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;
}