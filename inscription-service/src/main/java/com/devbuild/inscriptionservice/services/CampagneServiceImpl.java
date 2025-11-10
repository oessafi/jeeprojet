package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.dto.CreateCampagneRequest;
import com.devbuild.inscriptionservice.model.Campagne;
import com.devbuild.inscriptionservice.repository.CampagneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampagneServiceImpl implements CampagneService {

    private final CampagneRepository campagneRepository;

    @Override
    public Campagne createCampagne(CreateCampagneRequest request) {
        log.info("Création d'une nouvelle campagne pour {} {}", request.getType(), request.getAnneeAcademique());
        Campagne campagne = Campagne.builder()
                .anneeAcademique(request.getAnneeAcademique())
                .type(request.getType())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .active(false) // Par défaut, une campagne est créée "fermée"
                .build();
        return campagneRepository.save(campagne);
    }

    @Override
    public Campagne openCampagne(String id) {
        log.info("Ouverture de la campagne {}", id);
        Campagne campagne = getCampagneEntityById(id);

        // On ne peut pas ouvrir une campagne dont la date de fin est dépassée
        if (campagne.getDateFin().isBefore(LocalDateTime.now())) {
            log.error("Impossible d'ouvrir la campagne {}, la date de fin est dépassée.", id);
            throw new RuntimeException("Impossible d'ouvrir une campagne expirée.");
        }

        campagne.setActive(true);
        return campagneRepository.save(campagne);
    }

    @Override
    public Campagne closeCampagne(String id) {
        log.info("Fermeture de la campagne {}", id);
        Campagne campagne = getCampagneEntityById(id);
        campagne.setActive(false);
        return campagneRepository.save(campagne);
    }

    @Override
    public List<Campagne> getAllCampagnes() {
        log.info("Récupération de toutes les campagnes");
        return campagneRepository.findAll();
    }

    @Override
    public Campagne getCampagneById(String id) {
        return getCampagneEntityById(id);
    }

    private Campagne getCampagneEntityById(String id) {
        return campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée: " + id));
    }
}