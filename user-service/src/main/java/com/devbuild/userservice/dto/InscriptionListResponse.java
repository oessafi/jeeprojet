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
public class InscriptionListResponse {

    private boolean success;
    private String message;
    // On utilise Object ici car on n'a pas besoin du détail de l'inscription pour les stats
    // Cela évite de devoir copier InscriptionDTO, Enum Status, Enum Type etc. dans user-service
    private List<Object> data;
    private int total;
    private LocalDateTime timestamp;

    // Getter explicite pour boolean (Lombok le fait parfois mal avec "is")
    public boolean isSuccess() {
        return success;
    }
}