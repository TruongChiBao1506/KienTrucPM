package iuh.fit.se.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private Long userId;
    private String shippingAddress;
    private String paymentMethod;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderItemRequest> orderItems;
}
