package com.devbuild.soutenanceservice.dto;

import com.devbuild.soutenanceservice.enums.RoleJury;
import lombok.Data;

@Data
public class JuryMembreDTO {
    private String id;
    private String nomComplet;
    private String email;
    private String etablissement;
    private RoleJury role;
}