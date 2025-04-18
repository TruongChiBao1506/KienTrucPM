package iuh.fit.se.reviewservice.repository.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import iuh.fit.se.reviewservice.model.elasticsearch.ReviewDocument;

@Repository
public interface ReviewElasticsearchRepository extends ElasticsearchRepository<ReviewDocument, String> {
    
    // Find reviews by product ID
    Page<ReviewDocument> findByProductId(Long productId, Pageable pageable);
    
    // Find reviews by user ID
    Page<ReviewDocument> findByUserId(Long userId, Pageable pageable);
    
    // Search in content field
    Page<ReviewDocument> findByContentContaining(String keyword, Pageable pageable);
    
    // Custom query to search across multiple fields
    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"content\": \"?0\"}}," +
           "{\"match\": {\"username\": \"?0\"}}," +
           "{\"match\": {\"productName\": \"?0\"}}" +
           "]}}")
    Page<ReviewDocument> searchByKeyword(String keyword, Pageable pageable);
    
    // Find reviews by rating
    Page<ReviewDocument> findByRating(int rating, Pageable pageable);
    
    // Find reviews by rating greater than or equal to
    Page<ReviewDocument> findByRatingGreaterThanEqual(int rating, Pageable pageable);
}