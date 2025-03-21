package iuh.fit.se.reviewservice.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import iuh.fit.se.reviewservice.client.ProductClient;
import iuh.fit.se.reviewservice.client.UserClient;
import iuh.fit.se.reviewservice.dto.ProductDto;
import iuh.fit.se.reviewservice.dto.ReviewRequest;
import iuh.fit.se.reviewservice.dto.ReviewResponse;
import iuh.fit.se.reviewservice.dto.UserDto;
import iuh.fit.se.reviewservice.model.Review;
import iuh.fit.se.reviewservice.model.ReviewId;
import iuh.fit.se.reviewservice.repository.ReviewRepository;
import iuh.fit.se.reviewservice.service.ReviewService;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    @Override
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        // Lấy thông tin user và product từ các service khác
        UserDto user = userClient.getUserByUsername(reviewRequest.getUsername());
        ProductDto product = productClient.getProductById(reviewRequest.getProductId());

        // Tạo ReviewId
        ReviewId reviewId = new ReviewId(user.getId(), product.getId());

        // Tạo Review
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setContent(reviewRequest.getContent());
        review.setRating(reviewRequest.getRating());
        review.setCreatedAt(LocalDateTime.now());
        review.setUsername(user.getUsername());
        review.setProductName(product.getName());

        // Lưu review
        review = reviewRepository.save(review);

        // Chuyển đổi thành DTO phản hồi
        return mapToReviewResponse(review);
    }

    @Override
    public Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);

        return reviews.map(this::mapToReviewResponse);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setUserId(review.getReviewId().getUserId());
        response.setProductId(review.getReviewId().getProductId());
        response.setUsername(review.getUsername());
        response.setProductName(review.getProductName());
        response.setContent(review.getContent());
        response.setRating(review.getRating());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}