package iuh.fit.se.reviewservice.mapper;

import org.springframework.stereotype.Component;

import iuh.fit.se.reviewservice.dto.ReviewResponse;
import iuh.fit.se.reviewservice.model.Review;
import iuh.fit.se.reviewservice.model.elasticsearch.ReviewDocument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ReviewMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Convert from JPA entity to Elasticsearch document
     */
    public ReviewDocument toDocument(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewDocument.builder()
                .id(ReviewDocument.generateId(review.getReviewId().getUserId(), review.getReviewId().getProductId()))
                .userId(review.getReviewId().getUserId())
                .productId(review.getReviewId().getProductId())
                .username(review.getUsername())
                .productName(review.getProductName())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().format(DATE_FORMATTER) : null)
                .build();
    }

    /**
     * Convert from Elasticsearch document to ReviewResponse DTO
     */
    public ReviewResponse toDto(ReviewDocument document) {
        if (document == null) {
            return null;
        }

        ReviewResponse response = new ReviewResponse();
        response.setUserId(document.getUserId());
        response.setProductId(document.getProductId());
        response.setUsername(document.getUsername());
        response.setProductName(document.getProductName());
        response.setContent(document.getContent());
        response.setRating(document.getRating());

        // Convert String date back to LocalDateTime
        if (document.getCreatedAt() != null) {
            try {
                response.setCreatedAt(LocalDateTime.parse(document.getCreatedAt(), DATE_FORMATTER));
            } catch (Exception e) {
                // If parsing fails, set to null and log the error
                response.setCreatedAt(null);
                System.err.println("Error parsing date: " + document.getCreatedAt() + " - " + e.getMessage());
            }
        } else {
            response.setCreatedAt(null);
        }

        return response;
    }
}
