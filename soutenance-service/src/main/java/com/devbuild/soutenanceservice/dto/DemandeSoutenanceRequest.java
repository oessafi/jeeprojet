package com.devbuild.soutenanceservice.dto;

import lombok.Data;

@Data
public class DemandeSoutenanceRequest {
    private String doctorantId;
    private String inscriptionId;
    private Integer nbArticlesQ1Q2;
    private Integer nbConferences;
    private Integer nbCreditsFormation;
}