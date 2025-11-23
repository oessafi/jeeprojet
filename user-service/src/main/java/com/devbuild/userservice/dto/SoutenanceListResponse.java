package com.devbuild.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoutenanceListResponse {

    private boolean success;
    private String message;
    // Idem : Object suffit pour récupérer le "total"
    private List<Object> data;
    private int total;
    private LocalDateTime timestamp;

    public boolean isSuccess() {
        return success;
    }
}