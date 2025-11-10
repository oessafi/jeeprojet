package com.devbuild.inscriptionservice.controller;

import com.devbuild.inscriptionservice.dto.CreateCampagneRequest;
import com.devbuild.inscriptionservice.model.Campagne;
import com.devbuild.inscriptionservice.services.CampagneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/campagnes") // Un nouveau endpoint racine pour la gestion des campagnes
public class CampagneController {

    private final CampagneService campagneService;

    @PostMapping
    // TODO: Sécuriser cet endpoint (Admin seulement)
    public ResponseEntity<Campagne> createCampagne(@RequestBody CreateCampagneRequest request) {
        log.info("POST /campagnes - Création d'une campagne");
        Campagne campagne = campagneService.createCampagne(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(campagne);
    }

    @GetMapping
    // TODO: Sécuriser cet endpoint (Admin seulement)
    public ResponseEntity<List<Campagne>> getAllCampagnes() {
        log.info("GET /campagnes - Récupération de toutes les campagnes");
        return ResponseEntity.ok(campagneService.getAllCampagnes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campagne> getCampagneById(@PathVariable String id) {
        log.info("GET /campagnes/{}", id);
        return ResponseEntity.ok(campagneService.getCampagneById(id));
    }

    @PutMapping("/{id}/open")
    // TODO: Sécuriser cet endpoint (Admin seulement)
    public ResponseEntity<Campagne> openCampagne(@PathVariable String id) {
        log.info("PUT /campagnes/{}/open - Ouverture de la campagne", id);
        Campagne campagne = campagneService.openCampagne(id);
        return ResponseEntity.ok(campagne);
    }

    @PutMapping("/{id}/close")
    // TODO: Sécuriser cet endpoint (Admin seulement)
    public ResponseEntity<Campagne> closeCampagne(@PathVariable String id) {
        log.info("PUT /campagnes/{}/close - Fermeture de la campagne", id);
        Campagne campagne = campagneService.closeCampagne(id);
        return ResponseEntity.ok(campagne);
    }
}