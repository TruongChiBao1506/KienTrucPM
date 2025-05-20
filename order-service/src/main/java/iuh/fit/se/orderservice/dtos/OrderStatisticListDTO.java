package iuh.fit.se.orderservice.dtos;

import iuh.fit.se.orderservice.entities.OrderItem;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

public class OrderStatisticListDTO {
    private Long id;
    private String orderNumber;
    private UserDTO userId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private List<OrderItem> orderItems;
}
