package com.devbuild.inscriptionservice.enums;

public enum AnneeAcademique {
    ANNEE_2023_2024("2023-2024"),
    ANNEE_2024_2025("2024-2025"),
    ANNEE_2025_2026("2025-2026");

    private final String label;

    AnneeAcademique(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}