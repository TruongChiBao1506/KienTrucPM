package iuh.fit.se.orderservice.dtos;

import jakarta.annotation.security.DenyAll;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private UserDTO user;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private List<OrderItemUserDTO> orderItems;
}
