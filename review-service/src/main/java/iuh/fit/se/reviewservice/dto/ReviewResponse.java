package iuh.fit.se.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long userId;
    private Long productId;
    private String username;
    private String productName;
    private String content;
    private int rating;
    private LocalDateTime createdAt;
}