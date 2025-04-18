package iuh.fit.se.reviewservice.service;

import org.springframework.data.domain.Page;

import iuh.fit.se.reviewservice.dto.ReviewRequest;
import iuh.fit.se.reviewservice.dto.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest reviewRequest);

    Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int size);

    // Elasticsearch search methods
    Page<ReviewResponse> searchReviews(String keyword, int page, int size);

    Page<ReviewResponse> getReviewsByUserId(Long userId, int page, int size);

    Page<ReviewResponse> getReviewsByRating(int rating, int page, int size);

    Page<ReviewResponse> getReviewsByMinimumRating(int rating, int page, int size);
}
