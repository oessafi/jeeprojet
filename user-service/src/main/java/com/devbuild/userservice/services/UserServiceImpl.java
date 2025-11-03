package com.devbuild.userservice.services;

import com.devbuild.userservice.dto.*;
import com.devbuild.userservice.enums.UserRole;
import com.devbuild.userservice.enums.UserStatus;
import com.devbuild.userservice.model.User; // Importez l'entité
import com.devbuild.userservice.repository.UserRepository; // Importez le repository
import lombok.RequiredArgsConstructor; // Importez
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor // Utilise Lombok pour l'injection
public class UserServiceImpl implements UserService {

    // Injection via le constructeur (géré par Lombok)
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Nous n'avons plus besoin de userStore ou initializeTestData()

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs depuis la BDD");
        return userRepository.findAll()
                .stream()
                .map(this::mapUserToDTO) // Convertir chaque User en UserDTO
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

        // Créer l'entité User
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Crypter le mot de passe
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIF)
                .specialty(request.getSpecialty())
                .laboratory(request.getLaboratory())
                // createdAt et updatedAt seront gérés par @CreationTimestamp
                .build();

        if (request.getRole() == UserRole.CANDIDAT || request.getRole() == UserRole.DOCTORANT) {
            newUser.setStudentId(generateStudentId(request.getRole()));
        }

        // Sauvegarder dans la BDD
        User savedUser = userRepository.save(newUser);

        log.info("✅ Utilisateur créé avec succès: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        return mapUserToDTO(savedUser); // Retourner le DTO
    }

    @Override
    public UserDTO updateUser(String id, UpdateUserRequest request) {
        log.info("Mise à jour de l'utilisateur: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour les champs
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getSpecialty() != null) user.setSpecialty(request.getSpecialty());
        if (request.getLaboratory() != null) user.setLaboratory(request.getLaboratory());

        User updatedUser = userRepository.save(user); // Sauvegarder les modifications
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
        // Note: C'est inefficace. Mieux vaut ajouter une méthode au Repository
        // @Query("SELECT u FROM User u WHERE u.role = :role")
        // List<User> findByRole(@Param("role") UserRole role);

        // Pour l'instant, gardons la logique de filtrage en mémoire :
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
        UserDTO user = getUserById(id); // Réutilise la méthode qui retourne un DTO
        UserStatistics stats = generateStatistics(user.getRole());

        return UserProfileDTO.builder()
                .user(user)
                .statistics(stats)
                .build();
    }

    // --- Méthodes privées ---

    // Convertit une Entité User en UserDTO
    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                // Le mot de passe n'est PAS exposé dans le DTO
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
        // C'est aussi inefficace. Une séquence en BDD serait mieux.
        long count = userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .count() + 1;
        return String.format("%s%d%03d", prefix, year, count);
    }

    private UserStatistics generateStatistics(UserRole role) {
        // (Cette méthode reste inchangée)
        Random random = new Random();
        UserStatistics.UserStatisticsBuilder builder = UserStatistics.builder();
        switch (role) {
            case DOCTORANT:
                builder.totalInscriptions(random.nextInt(3) + 1)
                        .pendingDefenses(random.nextInt(2))
                        .completedDefenses(0);
                break;
            case DIRECTEUR_THESE:
                builder.totalDoctorants(random.nextInt(10) + 5)
                        .activeSupervisions(random.nextInt(5) + 2);
                break;
            case PERSONNEL_ADMIN:
                builder.totalValidations(random.nextInt(100) + 50)
                        .pendingRequests(random.nextInt(20) + 5);
                break;
            default:
                break;
        }
        return builder.build();
    }
}