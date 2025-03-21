package iuh.fit.se.reviewservice.service;

import org.springframework.data.domain.Page;

import iuh.fit.se.reviewservice.dto.ReviewRequest;
import iuh.fit.se.reviewservice.dto.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest reviewRequest);

    Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int size);
}