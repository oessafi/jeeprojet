package com.devbuild.soutenanceservice.model;

import com.devbuild.soutenanceservice.enums.SoutenanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demandes_soutenance")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemandeSoutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String doctorantId;
    @Column(nullable = false)
    private String inscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SoutenanceStatus status;

    // --- Validation des Prérequis ---
    @Column(nullable = false)
    private Integer nbArticlesQ1Q2;

    @Column(nullable = false)
    private Integer nbConferences;

    @Column(nullable = false)
    private Integer nbCreditsFormation;

    private boolean preRequisValidesAdmin;

    @OneToMany(mappedBy = "demandeSoutenance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SoutenanceDocument> documents;

    // --- AJOUTS ---
    @OneToMany(mappedBy = "demandeSoutenance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JuryMembre> membresJury;

    private LocalDateTime dateSoutenance;

    private String lieuSoutenance;


    @CreationTimestamp
    private LocalDateTime dateCreation;

    private String commentaireAdmin;
}