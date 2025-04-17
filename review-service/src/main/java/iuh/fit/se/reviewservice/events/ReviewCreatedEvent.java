package iuh.fit.se.reviewservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreatedEvent {
    private Long userId;
    private Long productId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
}