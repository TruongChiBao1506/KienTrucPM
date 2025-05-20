package iuh.fit.se.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Long id;
    private String message;
    private Long orderId;
    private LocalDateTime createdAt;
    private boolean isRead = false;
}
