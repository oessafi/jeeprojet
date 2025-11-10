package com.devbuild.soutenanceservice.model;

import com.devbuild.soutenanceservice.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "soutenance_documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SoutenanceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    private String fileName;
    private String contentType;
    private long fileSize;

    @Lob
    @Column(columnDefinition = "BYTEA")
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private DemandeSoutenance demandeSoutenance;
}