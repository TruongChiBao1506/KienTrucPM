package iuh.fit.se.emailservice.controllers;

import iuh.fit.se.emailservice.dtos.EmailRequest;
import iuh.fit.se.emailservice.dtos.EmailResponse;
import iuh.fit.se.emailservice.services.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from email service";
    }
    
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody EmailRequest emailRequest, 
                                                         BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        EmailResponse emailResponse;
        if (emailRequest.isHtml()) {
            emailResponse = emailService.sendHtmlEmail(emailRequest);
        } else {
            emailResponse = emailService.sendEmail(emailRequest);
        }
        
        response.put("status", HttpStatus.OK.value());
        response.put("data", emailResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-html")
    public ResponseEntity<Map<String, Object>> sendHtmlEmail(@Valid @RequestBody EmailRequest emailRequest,
                                                             BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Đảm bảo rằng email sẽ được gửi dưới dạng HTML
        emailRequest.setHtml(true);
        EmailResponse emailResponse = emailService.sendHtmlEmail(emailRequest);
        
        response.put("status", HttpStatus.OK.value());
        response.put("data", emailResponse);
        
        return ResponseEntity.ok(response);
    }
}
