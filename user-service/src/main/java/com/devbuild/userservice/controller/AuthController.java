package com.devbuild.userservice.controller;

import com.devbuild.userservice.config.JwtService;
import com.devbuild.userservice.dto.*;
import com.devbuild.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Endpoint pour l'enregistrement (création de compte)
    @PostMapping("/register")
    public ResponseEntity<UserResponseWrapper> register(
            @RequestBody CreateUserRequest request
    ) {
        UserDTO user = userService.createUser(request);
        UserResponseWrapper response = UserResponseWrapper.success(
                user,
                "Utilisateur créé avec succès"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Endpoint pour le login (authentification)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request
    ) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Si l'authentification réussit, générer un token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}