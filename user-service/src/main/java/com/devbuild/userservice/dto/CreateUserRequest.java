package com.devbuild.userservice.dto;

import com.devbuild.userservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String email;
   private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;

    // Champs optionnels selon le rôle
    private String specialty;
    private String laboratory;
}