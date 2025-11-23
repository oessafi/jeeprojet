package com.devbuild.notificationservice.consumer;

import com.devbuild.notificationservice.dto.EmailRequest;
import com.devbuild.notificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

	@Autowired
	private EmailService emailService;

	@KafkaListener(topics = "notification-topic", groupId = "notification-group")
	public void consume(EmailRequest emailRequest) {
		emailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
	}
}
