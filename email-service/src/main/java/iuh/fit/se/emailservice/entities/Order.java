package iuh.fit.se.emailservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private String orderNumber;
    private Long userId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
}
