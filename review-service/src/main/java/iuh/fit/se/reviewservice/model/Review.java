package iuh.fit.se.reviewservice.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {
    @EmbeddedId
    private ReviewId reviewId;

    private String content;
    private int rating;
    private LocalDateTime createdAt;

    // Thêm thông tin user và product (sẽ được lấy từ service khác)
    private String username;
    private String productName;
}