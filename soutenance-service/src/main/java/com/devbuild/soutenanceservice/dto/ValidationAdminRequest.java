package com.devbuild.soutenanceservice.dto;

import lombok.Data;

@Data
public class ValidationAdminRequest {
    private boolean approuvee;
    private String commentaire;
}