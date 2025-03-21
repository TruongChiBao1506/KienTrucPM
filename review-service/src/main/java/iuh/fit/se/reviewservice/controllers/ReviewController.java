package iuh.fit.se.reviewservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from review service";
    }
}
