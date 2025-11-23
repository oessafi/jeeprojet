package com.devbuild.notificationservice.controller;

import com.devbuild.notificationservice.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class KafkaTestController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaTestController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/kafka")
    public String sendTestMessage(@RequestBody EmailRequest emailRequest) {
        kafkaTemplate.send("notification-topic", emailRequest);
        return "Message published to notification-topic";
    }
}
