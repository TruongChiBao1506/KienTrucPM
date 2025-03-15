package iuh.fit.se.emailservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/send-email")
public class EmailController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from email service";
    }
}
