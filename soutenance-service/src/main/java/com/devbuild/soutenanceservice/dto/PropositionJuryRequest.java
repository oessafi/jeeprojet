package com.devbuild.soutenanceservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class PropositionJuryRequest {
    private List<JuryMembreDTO> membresJury;
}