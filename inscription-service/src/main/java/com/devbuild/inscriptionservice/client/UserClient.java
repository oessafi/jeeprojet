package com.devbuild.inscriptionservice.client;

import com.devbuild.inscriptionservice.dto.user.UserResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "user-service" est le nom de l'application enregistré dans Eureka
@FeignClient(name = "user-service")
public interface UserClient {

    // Cette signature doit correspondre au contrôleur de user-service
    // (voir: user-service/src/main/java/com/devbuild/userservice/controller/UserController.java)
    @GetMapping("/users/{id}")
    UserResponseWrapper getUserById(@PathVariable("id") String id);

    // Vous aurez besoin de copier les DTOs (comme UserResponseWrapper)
    // de "user-service" vers "inscription-service" pour que cela fonctionne.
}