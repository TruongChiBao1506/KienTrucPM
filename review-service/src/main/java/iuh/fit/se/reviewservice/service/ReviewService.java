package iuh.fit.se.reviewservice.service;

import org.springframework.data.domain.Page;

import iuh.fit.se.reviewservice.dto.ReviewRequest;
import iuh.fit.se.reviewservice.dto.ReviewResponse;

public interface ReviewService {
    /**
     * Create a new review
     * @param reviewRequest The review request containing review details
     * @return The created review response
     */
    ReviewResponse createReview(ReviewRequest reviewRequest);

    /**
     * Get reviews by product ID
     * @param productId The product ID to get reviews for
     * @param page The page number
     * @param size The page size
     * @return A page of review responses
     */
    Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int size);

    /**
     * Search for reviews by content
     * @param keyword The keyword to search for in review content
     * @param page The page number
     * @param size The page size
     * @return A page of review responses
     */
    Page<ReviewResponse> searchReviewsByContent(String keyword, int page, int size);

    /**
     * Search for reviews by product name
     * @param productName The product name to search for
     * @param page The page number
     * @param size The page size
     * @return A page of review responses
     */
    Page<ReviewResponse> searchReviewsByProductName(String productName, int page, int size);
}
