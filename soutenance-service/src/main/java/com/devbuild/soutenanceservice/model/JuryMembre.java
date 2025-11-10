package com.devbuild.soutenanceservice.model;

import com.devbuild.soutenanceservice.enums.RoleJury;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "jury_membres")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JuryMembre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nomComplet;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String etablissement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleJury role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private DemandeSoutenance demandeSoutenance;
}