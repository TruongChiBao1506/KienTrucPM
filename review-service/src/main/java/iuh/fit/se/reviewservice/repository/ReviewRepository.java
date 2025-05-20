package iuh.fit.se.reviewservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import iuh.fit.se.reviewservice.model.Review;
import iuh.fit.se.reviewservice.model.ReviewId;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {
    @Query(value = "select * from reviews where product_id = :id", nativeQuery = true)
    Page<Review> findByProductId(@Param("id") Long productId, Pageable pageable);
}