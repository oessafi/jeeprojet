package com.devbuild.userservice.services;

import com.devbuild.userservice.client.InscriptionClient;
import com.devbuild.userservice.client.SoutenanceClient;
import com.devbuild.userservice.dto.*;
import com.devbuild.userservice.enums.UserRole;
import com.devbuild.userservice.enums.UserStatus;
import com.devbuild.userservice.model.User;
import com.devbuild.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // --- Injection des Clients Feign ---
    private final InscriptionClient inscriptionClient;
    private final SoutenanceClient soutenanceClient;

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs depuis la BDD");
        return userRepository.findAll()
                .stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(String id) {
        log.info("Recherche de l'utilisateur avec ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapUserToDTO(user);
    }

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Email déjà existant: {}", request.getEmail());
            throw new RuntimeException("Email déjà utilisé");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIF)
                .specialty(request.getSpecialty())
                .laboratory(request.getLaboratory())
                .build();

        if (request.getRole() == UserRole.CANDIDAT || request.getRole() == UserRole.DOCTORANT) {
            newUser.setStudentId(generateStudentId(request.getRole()));
        }

        User savedUser = userRepository.save(newUser);
        log.info("✅ Utilisateur créé avec succès: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        return mapUserToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(String id, UpdateUserRequest request) {
        log.info("Mise à jour de l'utilisateur: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getSpecialty() != null) user.setSpecialty(request.getSpecialty());
        if (request.getLaboratory() != null) user.setLaboratory(request.getLaboratory());

        User updatedUser = userRepository.save(user);
        return mapUserToDTO(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        log.info("Suppression de l'utilisateur: {}", id);
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDTO> getUsersByRole(UserRole role) {
        log.info("Recherche des utilisateurs avec le rôle: {}", role);
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        log.info("Recherche de l'utilisateur par email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapUserToDTO(user);
    }

    @Override
    public UserDTO updateUserStatus(String id, UpdateStatusRequest request) {
        log.info("Changement de statut pour l'utilisateur: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setStatus(request.getStatus());
        User updatedUser = userRepository.save(user);

        return mapUserToDTO(updatedUser);
    }

    @Override
    public UserProfileDTO getUserProfile(String id) {
        log.info("Récupération du profil enrichi: {}", id);
        UserDTO user = getUserById(id);
        // Utilisation de l'ID réel de l'utilisateur pour récupérer ses stats
        UserStatistics stats = generateRealStatistics(user.getId(), user.getRole());

        return UserProfileDTO.builder()
                .user(user)
                .statistics(stats)
                .build();
    }

    @Override
    public UserDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapUserToDTO(user);
    }

    // --- Helpers ---

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .specialty(user.getSpecialty())
                .laboratory(user.getLaboratory())
                .studentId(user.getStudentId())
                .build();
    }

    private String generateStudentId(UserRole role) {
        String prefix = role == UserRole.CANDIDAT ? "C" : "D";
        int year = LocalDateTime.now().getYear();
        long count = userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .count() + 1;
        return String.format("%s%d%03d", prefix, year, count);
    }

    // Remplacement de l'ancienne méthode mockée par des appels réels
    private UserStatistics generateRealStatistics(String userId, UserRole role) {
        UserStatistics.UserStatisticsBuilder builder = UserStatistics.builder();

        // Initialisation des valeurs par défaut à 0
        builder.totalInscriptions(0)
                .pendingDefenses(0)
                .completedDefenses(0)
                .totalDoctorants(0)
                .activeSupervisions(0)
                .totalValidations(0)
                .pendingRequests(0);

        try {
            if (role == UserRole.DOCTORANT) {
                // 1. Appel à Inscription Service
                try {
                    InscriptionListResponse inscriptions = inscriptionClient.getInscriptionsByDoctorant(userId);
                    if (inscriptions != null && inscriptions.isSuccess()) {
                        builder.totalInscriptions(inscriptions.getTotal());
                    }
                } catch (Exception e) {
                    log.warn("Impossible de récupérer les inscriptions pour {}: {}", userId, e.getMessage());
                }

                // 2. Appel à Soutenance Service
                try {
                    SoutenanceListResponse soutenances = soutenanceClient.getSoutenancesByDoctorant(userId);
                    if (soutenances != null && soutenances.isSuccess()) {
                        builder.pendingDefenses(soutenances.getTotal());
                        // Ici on pourrait filtrer par statut pour affiner completedDefenses vs pendingDefenses
                    }
                } catch (Exception e) {
                    log.warn("Impossible de récupérer les soutenances pour {}: {}", userId, e.getMessage());
                }
            }
            // TODO: Ajouter la logique pour les Directeurs et Admins quand les endpoints seront dispos

        } catch (Exception e) {
            log.error("Erreur globale lors de la génération des statistiques pour {}", userId, e);
        }

        return builder.build();
    }
}