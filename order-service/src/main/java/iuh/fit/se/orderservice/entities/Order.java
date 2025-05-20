package iuh.fit.se.orderservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "order_date")
    private LocalDateTime orderDate;
    @Column(name = "total_amount")
    private Double totalAmount;
    private String status;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "shipping_address")
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}