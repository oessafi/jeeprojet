package com.devbuild.userservice.services;

import com.devbuild.userservice.dto.*;
import com.devbuild.userservice.enums.UserRole;
import com.devbuild.userservice.enums.UserStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final Map<String, UserDTO> userStore = new ConcurrentHashMap<>();

    public UserServiceImpl() {
        initializeTestData();
    }

    private void initializeTestData() {
        log.info("Initialisation des données de test...");

        // Candidat 1
        UserDTO candidat1 = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email("ahmed.benali@gmail.com")
                .firstName("Ahmed")
                .lastName("Benali")
                .phone("0612345678")
                .role(UserRole.CANDIDAT)
                .status(UserStatus.ACTIF)
                .createdAt(LocalDateTime.now().minusMonths(1))
                .updatedAt(LocalDateTime.now().minusMonths(1))
                .build();
        userStore.put(candidat1.getId(), candidat1);

        // Candidat 2
        UserDTO candidat2 = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email("fatima.zahrae@gmail.com")
                .firstName("Fatima Zahrae")
                .lastName("El Amrani")
                .phone("0698765432")
                .role(UserRole.CANDIDAT)
                .status(UserStatus.ACTIF)
                .createdAt(LocalDateTime.now().minusWeeks(2))
                .updatedAt(LocalDateTime.now().minusWeeks(2))
                .build();
        userStore.put(candidat2.getId(), candidat2);

        // Doctorant 1
        UserDTO doctorant1 = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email("youssef.idrissi@edu.ma")
                .firstName("Youssef")
                .lastName("Idrissi")
                .phone("0677889900")
                .role(UserRole.DOCTORANT)
                .status(UserStatus.ACTIF)
                .studentId("D2021001")
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();
        userStore.put(doctorant1.getId(), doctorant1);

        // Doctorant 2
        UserDTO doctorant2 = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email("karim.alaoui@edu.ma")
                .firstName("Karim")
                .lastName("Alaoui")
                .phone("0655443322")
                .role(UserRole.DOCTORANT)
                .status(UserStatus.ACTIF)
                .studentId("D2022015")
                .createdAt(LocalDateTime.now().minusYears(1))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build();
        userStore.put(doctorant2.getId(), doctorant2);

        // Directeur de thèse
        UserDTO directeur = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email("pr.benomar@univ.ma")
                .firstName("Mohammed")
                .lastName("Benomar")
                .phone("0661234567")
                .role(UserRole.DIRECTEUR_THESE)
                .status(UserStatus.ACTIF)
                .specialty("Intelligence Artificielle")
                .laboratory("Laboratoire Informatique et Systèmes")
                .createdAt(LocalDateTime.now().minusYears(5))
                .updatedAt(LocalDateTime.now().minusMonths(2))
                .build();
        userStore.put(directeur.getId(), directeur);

        // Personnel Admin
        UserDTO admin = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email("admin.doctorat@univ.ma")
                .firstName("Samira")
                .lastName("Tazi")
                .phone("0623456789")
                .role(UserRole.PERSONNEL_ADMIN)
                .status(UserStatus.ACTIF)
                .createdAt(LocalDateTime.now().minusYears(3))
                .updatedAt(LocalDateTime.now().minusWeeks(1))
                .build();
        userStore.put(admin.getId(), admin);

        //log.info(" {} utilisateurs de test initialisés", userStore.size());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        List<UserDTO> users = new ArrayList<>(userStore.values());
        log.info("{} utilisateurs récupérés", users.size());
        return users;
    }

    @Override
    public UserDTO getUserById(String id) {
        log.info("Recherche de l'utilisateur avec ID: {}", id);

        UserDTO user = userStore.get(id);
        if (user == null) {
            log.error("Utilisateur non trouvé: {}", id);
        }

        log.info("Utilisateur trouvé: {} {}", user.getFirstName(), user.getLastName());
        return user;
    }

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        boolean emailExists = userStore.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(request.getEmail()));

        if (emailExists) {
            log.error("Email déjà existant: {}", request.getEmail());
        }

        // Créer le nouvel utilisateur
        UserDTO newUser = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIF)
                .specialty(request.getSpecialty())
                .laboratory(request.getLaboratory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Générer un studentId pour les candidats/doctorants
        if (request.getRole() == UserRole.CANDIDAT || request.getRole() == UserRole.DOCTORANT) {
            newUser.setStudentId(generateStudentId(request.getRole()));
        }

        userStore.put(newUser.getId(), newUser);
        log.info("✅ Utilisateur créé avec succès: {} (ID: {})", newUser.getEmail(), newUser.getId());

        return newUser;
    }

    @Override
    public UserDTO updateUser(String id, UpdateUserRequest request) {
        log.info("Mise à jour de l'utilisateur: {}", id);

        UserDTO user = getUserById(id);

        // Mettre à jour les champs non nuls
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getSpecialty() != null) {
            user.setSpecialty(request.getSpecialty());
        }
        if (request.getLaboratory() != null) {
            user.setLaboratory(request.getLaboratory());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userStore.put(id, user);

        log.info("Utilisateur mis à jour: {}", id);
        return user;
    }

    @Override
    public void deleteUser(String id) {
        log.info("Suppression de l'utilisateur: {}", id);

        UserDTO user = getUserById(id);
        userStore.remove(id);

        log.info("Utilisateur supprimé: {} {}", user.getFirstName(), user.getLastName());
    }

    @Override
    public List<UserDTO> getUsersByRole(UserRole role) {
        log.info("Recherche des utilisateurs avec le rôle: {}", role);

        List<UserDTO> users = userStore.values().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());

        log.info("{} utilisateurs trouvés avec le rôle {}", users.size(), role);
        return users;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        log.info("Recherche de l'utilisateur par email: {}", email);

        UserDTO user = userStore.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'email: {}", email);
                    return null;
                });

        log.info("Utilisateur trouvé: {} {}", user.getFirstName(), user.getLastName());
        return user;
    }

    @Override
    public UserDTO updateUserStatus(String id, UpdateStatusRequest request) {
        log.info("Changement de statut pour l'utilisateur: {} vers {}", id, request.getStatus());

        UserDTO user = getUserById(id);
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        userStore.put(id, user);

        log.info("Statut mis à jour: {} -> {}", user.getEmail(), request.getStatus());
        return user;
    }

    @Override
    public UserProfileDTO getUserProfile(String id) {
        log.info("Récupération du profil enrichi: {}", id);

        UserDTO user = getUserById(id);

        // Créer des statistiques selon le rôle
        UserStatistics stats = generateStatistics(user.getRole());

        UserProfileDTO profile = UserProfileDTO.builder()
                .user(user)
                .statistics(stats)
                .build();

        log.info("Profil enrichi récupéré pour: {}", user.getEmail());
        return profile;
    }


    private String generateStudentId(UserRole role) {
        String prefix = role == UserRole.CANDIDAT ? "C" : "D";
        int year = LocalDateTime.now().getYear();
        int count = (int) userStore.values().stream()
                .filter(u -> u.getRole() == role)
                .count() + 1;
        return String.format("%s%d%03d", prefix, year, count);
    }

    private UserStatistics generateStatistics(UserRole role) {
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
                // Pas de statistiques pour les candidats
                break;
        }

        return builder.build();
    }
}