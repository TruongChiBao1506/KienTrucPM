package iuh.fit.se.reviewservice.controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iuh.fit.se.reviewservice.model.elasticsearch.ReviewDocument;
import iuh.fit.se.reviewservice.repository.elasticsearch.ReviewElasticsearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller riêng để test trực tiếp các phương thức của Elasticsearch Repository
 * Giúp kiểm tra xem Elasticsearch đã hoạt động đúng chưa
 */
@RestController
@RequestMapping("/api/es/test")
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchTestController {

    private final ReviewElasticsearchRepository elasticsearchRepository;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getElasticsearchInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Elasticsearch test controller is active");
        response.put("count", elasticsearchRepository.count());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> testFindByProductId(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Testing Elasticsearch findByProductId: {}", productId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDocument> results = elasticsearchRepository.findByProductId(productId, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> testFindByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Testing Elasticsearch findByUserId: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDocument> results = elasticsearchRepository.findByUserId(userId, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> testFindByContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Testing Elasticsearch findByContentContaining: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDocument> results = elasticsearchRepository.findByContentContaining(keyword, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> testSearchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Testing Elasticsearch searchByKeyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDocument> results = elasticsearchRepository.searchByKeyword(keyword, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<Map<String, Object>> testFindByRating(
            @PathVariable int rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Testing Elasticsearch findByRating: {}", rating);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDocument> results = elasticsearchRepository.findByRating(rating, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rating/min/{rating}")
    public ResponseEntity<Map<String, Object>> testFindByMinRating(
            @PathVariable int rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Testing Elasticsearch findByRatingGreaterThanEqual: {}", rating);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDocument> results = elasticsearchRepository.findByRatingGreaterThanEqual(rating, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Testing Elasticsearch findAll with pagination and sorting");
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReviewDocument> results = elasticsearchRepository.findAll(pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", results.getContent());
        response.put("totalElements", results.getTotalElements());
        response.put("totalPages", results.getTotalPages());
        response.put("currentPage", results.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewById(@PathVariable String id) {
        log.info("Testing Elasticsearch findById: {}", id);
        Optional<ReviewDocument> reviewOpt = elasticsearchRepository.findById(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());

        if (reviewOpt.isPresent()) {
            response.put("data", reviewOpt.get());
            response.put("message", "Review found");
        } else {
            response.put("message", "Review not found");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody ReviewDocument reviewDocument) {
        log.info("Testing Elasticsearch save: {}", reviewDocument);

        // Generate ID if not provided
        if (reviewDocument.getId() == null || reviewDocument.getId().isEmpty()) {
            reviewDocument.setId(ReviewDocument.generateId(reviewDocument.getUserId(), reviewDocument.getProductId()));
        }

        ReviewDocument savedReview = elasticsearchRepository.save(reviewDocument);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("data", savedReview);
        response.put("message", "Review created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable String id,
            @RequestBody ReviewDocument reviewDocument) {

        log.info("Testing Elasticsearch update: {}", id);
        Optional<ReviewDocument> existingReviewOpt = elasticsearchRepository.findById(id);

        Map<String, Object> response = new LinkedHashMap<>();

        if (existingReviewOpt.isPresent()) {
            // Ensure ID is preserved
            reviewDocument.setId(id);
            ReviewDocument updatedReview = elasticsearchRepository.save(reviewDocument);

            response.put("status", HttpStatus.OK.value());
            response.put("data", updatedReview);
            response.put("message", "Review updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Review not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable String id) {
        log.info("Testing Elasticsearch delete: {}", id);
        Optional<ReviewDocument> existingReviewOpt = elasticsearchRepository.findById(id);

        Map<String, Object> response = new LinkedHashMap<>();

        if (existingReviewOpt.isPresent()) {
            elasticsearchRepository.deleteById(id);

            response.put("status", HttpStatus.OK.value());
            response.put("message", "Review deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Review not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindexAll() {
        log.info("Testing Elasticsearch reindex operation");

        // This is a placeholder for a reindex operation
        // In a real application, this would typically involve:
        // 1. Fetching all reviews from the database
        // 2. Deleting the existing index
        // 3. Creating a new index
        // 4. Indexing all reviews

        long count = elasticsearchRepository.count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Reindex operation would be performed here");
        response.put("currentIndexSize", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint specifically for testing date conversion
     * This endpoint helps verify that our date conversion fix works correctly
     */
    @PostMapping("/test-date-conversion")
    public ResponseEntity<Map<String, Object>> testDateConversion(@RequestBody Map<String, String> request) {
        log.info("Testing date conversion with input: {}", request);

        String dateString = request.getOrDefault("date", "2025-04-18");

        try {
            // Create a review document with the test date
            ReviewDocument review = ReviewDocument.builder()
                .id("test-date-conversion")
                .userId(1L)
                .productId(1L)
                .username("Test User")
                .productName("Test Product")
                .content("Test content for date conversion")
                .rating(5)
                .createdAt(dateString) // Set the date string directly
                .build();

            // Let Spring Data Elasticsearch handle the conversion
            Map<String, Object> rawData = new LinkedHashMap<>();
            rawData.put("id", review.getId());
            rawData.put("userId", review.getUserId());
            rawData.put("productId", review.getProductId());
            rawData.put("username", review.getUsername());
            rawData.put("productName", review.getProductName());
            rawData.put("content", review.getContent());
            rawData.put("rating", review.getRating());
            rawData.put("createdAt", dateString);  // This will trigger the conversion

            // Convert the raw data to JSON and back to a ReviewDocument to test conversion
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(rawData);
            log.info("Test JSON: {}", json);

            // Save the review to Elasticsearch
            ReviewDocument savedReview = elasticsearchRepository.save(review);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Date conversion test completed");
            response.put("inputDate", dateString);
            response.put("testJson", json);
            response.put("savedReview", savedReview);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing date conversion", e);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Date conversion test failed");
            response.put("error", e.getMessage());
            response.put("inputDate", dateString);

            return ResponseEntity.badRequest().body(response);
        }
    }
}
