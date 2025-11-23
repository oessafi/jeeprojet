package com.devbuild.userservice.client;

import com.devbuild.userservice.dto.SoutenanceListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "soutenance-service")
public interface SoutenanceClient {

    @GetMapping("/soutenances/doctorant/{doctorantId}")
    SoutenanceListResponse getSoutenancesByDoctorant(@PathVariable("doctorantId") String doctorantId);
}
