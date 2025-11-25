package com.devbuild.notificationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class KafkaTestController {
	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaTestController(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@PostMapping("/kafka/test")
	public ResponseEntity<String> publishTest(@RequestParam(defaultValue = "hello") String msg) {
		kafkaTemplate.send("notifications", msg);
		return ResponseEntity.ok("sent");
	}
}
