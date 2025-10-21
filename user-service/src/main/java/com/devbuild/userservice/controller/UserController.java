package com.devbuild.userservice.controller;

import com.devbuild.userservice.dto.*;
import com.devbuild.userservice.enums.UserRole;
import com.devbuild.userservice.services.UserService;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ===================================================================
    // ENDPOINTS CRUD DE BASE
    // ===================================================================


    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers() {
        log.info("GET /users - Récupération de tous les utilisateurs");
        List<UserDTO> users = userService.getAllUsers();
        UserListResponse response = UserListResponse.success(
                users,
                "Liste des utilisateurs récupérée avec succès"
        );

        log.info("Réponse: {} utilisateurs trouvés", users.size());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseWrapper> getUserById(@PathVariable String id) {
        log.info("📥 GET /users/{} - Récupération d'un utilisateur", id);

        UserDTO user = userService.getUserById(id);
        UserResponseWrapper response = UserResponseWrapper.success(
                user,
                "Utilisateur récupéré avec succès"
        );

        log.info("📤 Réponse: Utilisateur {} {} trouvé", user.getFirstName(), user.getLastName());
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<UserResponseWrapper> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("POST /users - Création d'un utilisateur: {}", request.getEmail());

        UserDTO user = userService.createUser(request);
        UserResponseWrapper response = UserResponseWrapper.success(
                user,
                "Utilisateur créé avec succès"
        );

        log.info("Réponse: Utilisateur créé avec ID: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponseWrapper> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("PUT /users/{} - Mise à jour d'un utilisateur", id);

        UserDTO user = userService.updateUser(id, request);
        UserResponseWrapper response = UserResponseWrapper.success(
                user,
                "Utilisateur mis à jour avec succès"
        );

        log.info("📤 Réponse: Utilisateur {} mis à jour", id);
        return ResponseEntity.ok(response);
    }

    /**

/*    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable String id) {
        log.info(" DELETE /users/{} - Suppression d'un utilisateur", id);

        userService.deleteUser(id);
        MessageResponse response = MessageResponse.success(
                "Utilisateur supprimé avec succès"
        );

        log.info("Réponse: Utilisateur {} supprimé", id);
        return ResponseEntity.ok(response);
    }*/

    // ===================================================================
    // ENDPOINTS DE RECHERCHE
    // ===================================================================

    @GetMapping("/search")
    public ResponseEntity<UserResponseWrapper> getUserByEmail(
            @RequestParam String email) {

        log.info("📥 GET /users/search?email={} - Recherche par email", email);

        UserDTO user = userService.getUserByEmail(email);
        UserResponseWrapper response = UserResponseWrapper.success(
                user,
                "Utilisateur trouvé"
        );

        log.info("📤 Réponse: Utilisateur {} trouvé", email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<UserListResponse> getUsersByRole(
            @PathVariable UserRole role) {

        log.info("GET /users/role/{} - Filtrage par rôle", role);

        List<UserDTO> users = userService.getUsersByRole(role);
        UserListResponse response = UserListResponse.success(
                users,
                "Utilisateurs filtrés par rôle: " + role
        );

        log.info("Réponse: {} utilisateurs avec le rôle {}", users.size(), role);
        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // ENDPOINTS SPÉCIFIQUES AU CONTEXTE DOCTORAL
    // ===================================================================

    @GetMapping("/doctorants")
    public ResponseEntity<UserListResponse> getAllDoctorants() {
        log.info("GET /users/doctorants - Récupération de tous les doctorants");

        List<UserDTO> doctorants = userService.getUsersByRole(UserRole.DOCTORANT);
        UserListResponse response = UserListResponse.success(
                doctorants,
                "Liste des doctorants récupérée"
        );

        log.info("Réponse: {} doctorants trouvés", doctorants.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /users/candidats - Liste tous les candidats
     */
    @GetMapping("/candidats")
    public ResponseEntity<UserListResponse> getAllCandidats() {
        log.info("GET /users/candidats - Récupération de tous les candidats");

        List<UserDTO> candidats = userService.getUsersByRole(UserRole.CANDIDAT);
        UserListResponse response = UserListResponse.success(
                candidats,
                "Liste des candidats récupérée"
        );

        log.info("Réponse: {} candidats trouvés", candidats.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/directeurs")
    public ResponseEntity<UserListResponse> getAllDirecteurs() {
        log.info("GET /users/directeurs - Récupération de tous les directeurs");

        List<UserDTO> directeurs = userService.getUsersByRole(UserRole.DIRECTEUR_THESE);
        UserListResponse response = UserListResponse.success(
                directeurs,
                "Liste des directeurs de thèse récupérée"
        );

        log.info("📤 Réponse: {} directeurs trouvés", directeurs.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<UserListResponse> getAllAdmins() {
        log.info("GET /users/admin - Récupération du personnel administratif");

        List<UserDTO> admins = userService.getUsersByRole(UserRole.PERSONNEL_ADMIN);
        UserListResponse response = UserListResponse.success(
                admins,
                "Liste du personnel administratif récupérée"
        );

        log.info("📤 Réponse: {} administrateurs trouvés", admins.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String id) {
        log.info("GET /users/{}/profile - Récupération du profil enrichi", id);

        UserProfileDTO profile = userService.getUserProfile(id);

        log.info("Réponse: Profil de {} récupéré", profile.getUser().getEmail());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponseWrapper> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request) {

        log.info("PUT /users/{}/status - Changement de statut vers: {}", id, request.getStatus());

        UserDTO user = userService.updateUserStatus(id, request);
        UserResponseWrapper response = UserResponseWrapper.success(
                user,
                "Statut mis à jour avec succès vers: " + request.getStatus()
        );

        log.info("Réponse: Statut de {} changé vers {}", id, request.getStatus());
        return ResponseEntity.ok(response);
    }

}
