package iuh.fit.se.notificationservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class notificationController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from notification service";
    }
}
