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
import iuh.fit.se.reviewservice.mapper.ReviewMapper;
import iuh.fit.se.reviewservice.model.Review;
import iuh.fit.se.reviewservice.model.ReviewId;
import iuh.fit.se.reviewservice.model.elasticsearch.ReviewDocument;
import iuh.fit.se.reviewservice.repository.ReviewRepository;
import iuh.fit.se.reviewservice.repository.elasticsearch.ReviewElasticsearchRepository;
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
    private final ReviewElasticsearchRepository elasticsearchRepository;
    private final ReviewMapper reviewMapper;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;

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
                ReviewDocument document = reviewMapper.toDocument(review);
                elasticsearchRepository.save(document);
                log.info("Đã lưu review vào Elasticsearch thành công");
            } catch (Exception e) {
                log.error("Lỗi khi lưu review vào Elasticsearch: {}", e.getMessage(), e);
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

    @Override
    public Page<ReviewResponse> searchReviews(String keyword, int page, int size) {
        log.info("Tìm kiếm review với từ khóa: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<ReviewDocument> searchResults = elasticsearchRepository.searchByKeyword(keyword, pageable);
            return searchResults.map(reviewMapper::toDto);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm review trong Elasticsearch: {}", e.getMessage(), e);
            // Fallback to database search if Elasticsearch fails
            // Since there's no direct method in JPA repository for full-text search,
            // we'll just return all reviews and log a warning
            log.warn("Fallback to database without full-text search capability");
            Page<Review> reviews = reviewRepository.findAll(pageable);
            return reviews.map(this::mapToReviewResponse);
        }
    }

    @Override
    public Page<ReviewResponse> getReviewsByUserId(Long userId, int page, int size) {
        log.info("Lấy review của user ID: {}", userId);
        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<ReviewDocument> searchResults = elasticsearchRepository.findByUserId(userId, pageable);
            return searchResults.map(reviewMapper::toDto);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm review theo user ID trong Elasticsearch: {}", e.getMessage(), e);
            // Fallback to database
            // Since there's no direct method in JPA repository, we need to implement a custom query
            // For now, return empty page
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<ReviewResponse> getReviewsByRating(int rating, int page, int size) {
        log.info("Lấy review có rating: {}", rating);
        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<ReviewDocument> searchResults = elasticsearchRepository.findByRating(rating, pageable);
            return searchResults.map(reviewMapper::toDto);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm review theo rating trong Elasticsearch: {}", e.getMessage(), e);
            // Fallback to database
            // Since there's no direct method in JPA repository, we need to implement a custom query
            // For now, return empty page
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<ReviewResponse> getReviewsByMinimumRating(int rating, int page, int size) {
        log.info("Lấy review có rating >= {}", rating);
        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<ReviewDocument> searchResults = elasticsearchRepository.findByRatingGreaterThanEqual(rating, pageable);
            return searchResults.map(reviewMapper::toDto);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm review theo minimum rating trong Elasticsearch: {}", e.getMessage(), e);
            // Fallback to database
            // Since there's no direct method in JPA repository, we need to implement a custom query
            // For now, return empty page
            return Page.empty(pageable);
        }
    }
}
