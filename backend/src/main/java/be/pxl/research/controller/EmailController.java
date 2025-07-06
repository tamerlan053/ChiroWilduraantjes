package be.pxl.research.controller;

import be.pxl.research.controller.request.EmailRequest;
import be.pxl.research.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequest emailRequest) {
        emailService.sendEmail(emailRequest.to(), emailRequest.subject(), emailRequest.body());
        return ResponseEntity.ok("Email sent");
    }


}
