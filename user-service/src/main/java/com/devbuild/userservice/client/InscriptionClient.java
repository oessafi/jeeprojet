package com.devbuild.userservice.client;

import com.devbuild.userservice.dto.InscriptionListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inscription-service")
public interface InscriptionClient {

    @GetMapping("/inscriptions/doctorant/{doctorantId}")
    InscriptionListResponse getInscriptionsByDoctorant(@PathVariable("doctorantId") String doctorantId);
}
