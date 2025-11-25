package com.devbuild.userservice.client;

import com.devbuild.userservice.dto.InscriptionListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Ensure "inscription-service" matches the spring.application.name of your other microservice
@FeignClient(name = "inscription-service")
public interface InscriptionClient {

    // This defines the method that was missing in your error log
    @GetMapping("/api/inscriptions/doctorant/{id}")
    InscriptionListResponse getInscriptionsByDoctorant(@PathVariable("id") String id);

}