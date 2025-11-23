package com.devbuild.notificationservice.controller;

import com.devbuild.notificationservice.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate; // Import Kafka
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	// On injecte le Template Kafka à la place du EmailService direct
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	// Nom de votre topic (doit être le même que dans le Consumer)
	private static final String TOPIC_NAME = "notification-topic";

	@PostMapping("/send-email")
	public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
		try {
			// 1. On envoie la demande à Kafka (C'est très rapide)
			kafkaTemplate.send(TOPIC_NAME, emailRequest);

			// 2. On répond tout de suite au client (Soutenance Service)
			return ResponseEntity.ok("✅ SUCCÈS : Demande de notification reçue et transmise à Kafka pour " + emailRequest.getTo());

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("❌ ERREUR KAFKA : " + e.getMessage());
		}
	}
}