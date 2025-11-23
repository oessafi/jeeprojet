package com.devbuild.soutenanceservice.client;

import com.devbuild.soutenanceservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// "notification-service" est le nom Eureka du service que nous créons dans la Partie 2
@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/send-email")
    void sendNotification(@RequestBody EmailRequest request);
}