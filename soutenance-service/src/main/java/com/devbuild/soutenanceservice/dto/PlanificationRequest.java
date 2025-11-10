package com.devbuild.soutenanceservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlanificationRequest {
    private LocalDateTime dateSoutenance;
    private String lieuSoutenance;
}