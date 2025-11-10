package com.devbuild.inscriptionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String fileName;
    private String contentType;
    private long fileSize;

    @Lob // Indique que c'est un "Large Object" (pour les données binaires)
    @Column(columnDefinition = "BYTEA") // Spécifique à PostgreSQL pour les octets
    @Basic(fetch = FetchType.LAZY) // Ne pas charger le fichier binaire sauf si on le demande
    @JsonIgnore // Ne jamais exposer les données binaires dans les DTOs
    private byte[] data;

    // --- La relation ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscription_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Si l'inscription est supprimée, supprime les docs
    @JsonIgnore // Empêche les boucles infinies lors de la sérialisation
    private Inscription inscription;
}