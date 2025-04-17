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
import iuh.fit.se.reviewservice.events.ReviewCreatedEvent;
import iuh.fit.se.reviewservice.kafka.KafkaConsumerService;
import iuh.fit.se.reviewservice.kafka.KafkaProducerService;
import iuh.fit.se.reviewservice.model.Review;
import iuh.fit.se.reviewservice.model.ReviewId;
import iuh.fit.se.reviewservice.model.elasticsearch.ReviewDocument;
import iuh.fit.se.reviewservice.repository.ReviewRepository;
import iuh.fit.se.reviewservice.service.ElasticsearchService;
import iuh.fit.se.reviewservice.service.ReviewService;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;
    private final ElasticsearchService elasticsearchService;

    @Override
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        if (reviewRequest.getProductId() == null) {
            throw new IllegalArgumentException("ProductId không được để trống");
        }

        if (reviewRequest.getUsername() == null || reviewRequest.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }

        try {
            // Try to get user from Kafka cache first
            UserDto user = kafkaConsumerService.getUserByUsername(reviewRequest.getUsername());

            // If not in cache, fall back to synchronous call
            if (user == null) {
                log.info("User not found in cache, calling user-service: {}", reviewRequest.getUsername());
                Map<String, Object> userResponse = userClient.getUserByUsername(reviewRequest.getUsername());
                user = convertToUserDto(userResponse);
            } else {
                log.info("User found in Kafka cache: {}", user.getUsername());
            }

            if (user == null || user.getId() == null) {
                throw new IllegalStateException("Không thể lấy thông tin người dùng từ user-service");
            }

            // Try to get product from Kafka cache first
            ProductDto product = kafkaConsumerService.getProductById(reviewRequest.getProductId());

            // If not in cache, fall back to synchronous call
            if (product == null) {
                log.info("Product not found in cache, calling product-service: {}", reviewRequest.getProductId());
                Map<String, Object> productResponse = productClient.getProductById(reviewRequest.getProductId());
                product = convertToProductDto(productResponse);
            } else {
                log.info("Product found in Kafka cache: {}", product.getName());
            }

            if (product == null || product.getId() == null) {
                throw new IllegalStateException("Không thể lấy thông tin sản phẩm từ product-service");
            }

            log.info("Đã lấy được thông tin: User ID={}, Product ID={}", user.getId(), product.getId());

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
            log.info("Đang lưu review cho sản phẩm ID={} bởi user ID={}", product.getId(), user.getId());
            review = reviewRepository.save(review);
            log.info("Đã lưu review thành công");

            // Index review in Elasticsearch
            try {
                log.info("Indexing review in Elasticsearch");
                elasticsearchService.indexReview(review);
                log.info("Review indexed successfully in Elasticsearch");
            } catch (Exception e) {
                log.error("Failed to index review in Elasticsearch: {}", e.getMessage(), e);
                // Continue execution even if Elasticsearch indexing fails
            }

            // Send review created event to Kafka
            ReviewCreatedEvent event = new ReviewCreatedEvent(
                user.getId(),
                product.getId(),
                user.getUsername(),
                reviewRequest.getContent(),
                reviewRequest.getRating(),
                review.getCreatedAt()
            );
            kafkaProducerService.sendReviewCreatedEvent(event);

            // Chuyển đổi thành DTO phản hồi
            return mapToReviewResponse(review);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý review: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo review: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi thủ công từ Map response sang UserDto
     */
    @SuppressWarnings("unchecked")
    private UserDto convertToUserDto(Map<String, Object> userResponse) {
        try {
            // Lấy data từ response
            Map<String, Object> userData = (Map<String, Object>) userResponse.get("data");
            if (userData == null) {
                log.error("Không tìm thấy dữ liệu người dùng trong phản hồi");
                return null;
            }

            UserDto userDto = new UserDto();

            // Nếu user-service trả về userId thay vì id
            if (userData.containsKey("userId")) {
                userDto.setId(Long.valueOf(userData.get("userId").toString()));
            } else if (userData.containsKey("id")) {
                userDto.setId(Long.valueOf(userData.get("id").toString()));
            }

            if (userData.containsKey("username")) {
                userDto.setUsername(userData.get("username").toString());
            }

            if (userData.containsKey("email")) {
                userDto.setEmail(userData.get("email").toString());
            }

            return userDto;
        } catch (Exception e) {
            log.error("Lỗi khi chuyển đổi dữ liệu người dùng: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Chuyển đổi thủ công từ Map response sang ProductDto
     */
    @SuppressWarnings("unchecked")
    private ProductDto convertToProductDto(Map<String, Object> productResponse) {
        try {
            // Lấy data từ response
            Map<String, Object> productData = (Map<String, Object>) productResponse.get("data");
            if (productData == null) {
                log.error("Không tìm thấy dữ liệu sản phẩm trong phản hồi");
                return null;
            }

            ProductDto productDto = new ProductDto();

            if (productData.containsKey("id")) {
                productDto.setId(Long.valueOf(productData.get("id").toString()));
            }

            if (productData.containsKey("name")) {
                productDto.setName(productData.get("name").toString());
            }

            if (productData.containsKey("description")) {
                productDto.setDescription(productData.get("description").toString());
            }

            if (productData.containsKey("price")) {
                String priceStr = productData.get("price").toString();
                productDto.setPrice(Double.parseDouble(priceStr));
            }

            return productDto;
        } catch (Exception e) {
            log.error("Lỗi khi chuyển đổi dữ liệu sản phẩm: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        try {
            // Try to get reviews from Elasticsearch first
            log.info("Searching for reviews in Elasticsearch by productId: {}", productId);
            Page<ReviewDocument> esReviews = elasticsearchService.findByProductId(productId, pageable);

            if (esReviews != null && esReviews.hasContent()) {
                log.info("Found {} reviews in Elasticsearch", esReviews.getTotalElements());
                return esReviews.map(this::mapToReviewResponse);
            } else {
                log.info("No reviews found in Elasticsearch, falling back to database");
            }
        } catch (Exception e) {
            log.error("Error searching in Elasticsearch: {}", e.getMessage(), e);
            log.info("Falling back to database search");
        }

        // Fall back to database if Elasticsearch search fails or returns no results
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    /**
     * Maps an Elasticsearch ReviewDocument to a ReviewResponse
     */
    private ReviewResponse mapToReviewResponse(ReviewDocument document) {
        ReviewResponse response = new ReviewResponse();
        response.setUserId(document.getUserId());
        response.setProductId(document.getProductId());
        response.setUsername(document.getUsername());
        response.setProductName(document.getProductName());
        response.setContent(document.getContent());
        response.setRating(document.getRating());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }

    @Override
    public Page<ReviewResponse> searchReviewsByContent(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        try {
            log.info("Searching for reviews in Elasticsearch by content: {}", keyword);
            Page<ReviewDocument> esReviews = elasticsearchService.searchByContent(keyword, pageable);

            if (esReviews != null && esReviews.hasContent()) {
                log.info("Found {} reviews in Elasticsearch", esReviews.getTotalElements());
                return esReviews.map(this::mapToReviewResponse);
            } else {
                log.info("No reviews found in Elasticsearch for content: {}", keyword);
                // Return empty page if no results found
                return Page.empty(pageable);
            }
        } catch (Exception e) {
            log.error("Error searching in Elasticsearch by content: {}", e.getMessage(), e);
            // Return empty page on error
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<ReviewResponse> searchReviewsByProductName(String productName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        try {
            log.info("Searching for reviews in Elasticsearch by product name: {}", productName);
            Page<ReviewDocument> esReviews = elasticsearchService.searchByProductName(productName, pageable);

            if (esReviews != null && esReviews.hasContent()) {
                log.info("Found {} reviews in Elasticsearch", esReviews.getTotalElements());
                return esReviews.map(this::mapToReviewResponse);
            } else {
                log.info("No reviews found in Elasticsearch for product name: {}", productName);
                // Return empty page if no results found
                return Page.empty(pageable);
            }
        } catch (Exception e) {
            log.error("Error searching in Elasticsearch by product name: {}", e.getMessage(), e);
            // Return empty page on error
            return Page.empty(pageable);
        }
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
