package iuh.fit.se.reviewservice.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.reviewservice.dto.ReviewRequest;
import iuh.fit.se.reviewservice.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody ReviewRequest reviewRequest) {
        System.out.println("Received review request: " + reviewRequest);
        var createdReview = reviewService.createReview(reviewRequest);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", createdReview);
        response.put("message", "Tạo review thành công!");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getReviews")
    public ResponseEntity<Map<String, Object>> getReviewByProduct(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        var reviewsPage = reviewService.getReviewsByProductId(productId, page, size);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", reviewsPage.getContent());
        response.put("hasMore", !reviewsPage.isLast());
        response.put("totalElements", reviewsPage.getTotalElements());
        response.put("totalPages", reviewsPage.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}