package com.devbuild.inscriptionservice.services;

import com.devbuild.inscriptionservice.dto.CreateCampagneRequest;
import com.devbuild.inscriptionservice.model.Campagne;

import java.util.List;

public interface CampagneService {
    Campagne createCampagne(CreateCampagneRequest request);
    Campagne openCampagne(String id);
    Campagne closeCampagne(String id);
    List<Campagne> getAllCampagnes();
    Campagne getCampagneById(String id);
}