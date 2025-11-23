package com.devbuild.notificationservice.controller;

import com.devbuild.notificationservice.dto.EmailRequest;
import com.devbuild.notificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	@Autowired
	private EmailService emailService;

	@PostMapping("/send-email")
	public String sendEmail(@RequestBody EmailRequest emailRequest) {
		emailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
		return "Email sent successfully!";
	}
}
