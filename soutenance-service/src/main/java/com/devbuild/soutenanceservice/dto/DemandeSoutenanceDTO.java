package com.devbuild.soutenanceservice.dto;

import com.devbuild.soutenanceservice.enums.SoutenanceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DemandeSoutenanceDTO {
    private String id;
    private String doctorantId;
    private SoutenanceStatus status;
    private Integer nbArticlesQ1Q2;
    private Integer nbConferences;
    private Integer nbCreditsFormation;
    private boolean preRequisValidesAdmin;
    private LocalDateTime dateCreation;
    private String commentaireAdmin;
    private List<DocumentInfoDTO> documents;
}