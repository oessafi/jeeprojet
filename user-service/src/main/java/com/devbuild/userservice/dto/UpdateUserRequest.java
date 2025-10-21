package com.devbuild.userservice.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String firstName;
    private String lastName;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Numéro de téléphone invalide")
    private String phone;

    private String specialty;
    private String laboratory;
}