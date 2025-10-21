package com.devbuild.inscriptionservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInscriptionRequest {

    private String sujetThese;
    private String laboratoire;
    private String specialite;
    private String coDirecteurId;
}