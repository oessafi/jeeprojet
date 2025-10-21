package com.devbuild.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    // Pour doctorant
    private Integer totalInscriptions;
    private Integer pendingDefenses;
    private Integer completedDefenses;

    // Pour directeur de thèse
    private Integer totalDoctorants;
    private Integer activeSupervisions;

    // Pour admin
    private Integer totalValidations;
    private Integer pendingRequests;
}